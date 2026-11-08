package com.sangreyoro.matching.presentation;

import com.sangreyoro.matching.business.MatchingService;
import com.sangreyoro.matching.business.translation.DescriptionView;
import com.sangreyoro.matching.business.translation.ProfileDescriptionService;
import com.sangreyoro.matching.data.NotificationRepository;
import com.sangreyoro.matching.data.UserRepository;
import com.sangreyoro.matching.domain.MatchCandidate;
import com.sangreyoro.matching.domain.MatchCriterion;
import com.sangreyoro.matching.domain.MatchFilter;
import com.sangreyoro.matching.domain.MatchRequest;
import com.sangreyoro.matching.domain.MatchStatus;
import com.sangreyoro.matching.domain.Notification;
import com.sangreyoro.matching.domain.Student;
import com.sangreyoro.matching.domain.StudentType;
import com.sangreyoro.matching.exception.MatchingException;
import com.sangreyoro.matching.util.Json;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Presentation Layer – REST-style JSON API of UC2 (SDD 7.3, 11.1). It translates HTTP into calls to the
 * Business Layer and business exceptions into HTTP status codes; it contains no matching logic.
 */
public class MatchController implements HttpHandler {
    private static final Logger LOG = Logger.getLogger(MatchController.class.getName());
    private static final Pattern ANSWER = Pattern.compile("^/api/requests/([A-Za-z0-9-]+)/(accept|reject)$");
    private static final Pattern DESCRIPTION = Pattern.compile("^/api/students/([A-Za-z0-9-]+)/description$");

    private final MatchingService matching;
    private final ProfileDescriptionService descriptions;
    private final UserRepository users;
    private final NotificationRepository notifications;

    public MatchController(MatchingService matching, ProfileDescriptionService descriptions, UserRepository users,
                           NotificationRepository notifications) {
        this.matching = matching;
        this.descriptions = descriptions;
        this.users = users;
        this.notifications = notifications;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        String method = ex.getRequestMethod();
        long start = System.nanoTime();
        int status = 200;
        try {
            Matcher m;
            if (method.equals("GET") && path.equals("/api/students")) {
                HttpExchangeHelper.json(ex, 200, Json.write(students()));
            } else if (method.equals("GET") && path.equals("/api/criteria")) {
                HttpExchangeHelper.json(ex, 200, Json.write(criteria()));
            } else if (method.equals("GET") && path.equals("/api/matches")) {
                HttpExchangeHelper.json(ex, 200, Json.write(findMatches(HttpExchangeHelper.query(ex))));
            } else if (method.equals("POST") && path.equals("/api/requests")) {
                Map<String, String> f = HttpExchangeHelper.form(ex);
                MatchRequest r = matching.sendRequest(required(f, "requesterId"), required(f, "candidateId"),
                        MatchCriterion.parse(required(f, "criterion")));
                status = 201;
                HttpExchangeHelper.json(ex, 201, Json.write(request(r, r.requesterId())));
            } else if (method.equals("GET") && path.equals("/api/requests")) {
                String id = required(HttpExchangeHelper.query(ex), "studentId");
                List<Object> out = new ArrayList<>();
                for (MatchRequest r : matching.requestsOf(id)) out.add(request(r, id));
                HttpExchangeHelper.json(ex, 200, Json.write(out));
            } else if (method.equals("POST") && (m = ANSWER.matcher(path)).matches()) {
                Map<String, String> f = HttpExchangeHelper.form(ex);
                String who = required(f, "studentId");
                MatchRequest r = matching.respond(m.group(1), who, m.group(2).equals("accept"));
                HttpExchangeHelper.json(ex, 200, Json.write(request(r, who)));
            } else if (method.equals("GET") && path.equals("/api/notifications")) {
                HttpExchangeHelper.json(ex, 200, Json.write(notifications(required(HttpExchangeHelper.query(ex), "studentId"))));
            } else if (method.equals("GET") && (m = DESCRIPTION.matcher(path)).matches()) {
                String lang = HttpExchangeHelper.query(ex).getOrDefault("lang", "en");
                DescriptionView d = descriptions.describe(m.group(1), lang);
                Map<String, Object> o = new LinkedHashMap<>();
                o.put("text", d.text());
                o.put("language", d.language());
                o.put("translated", d.translated());
                o.put("notice", d.notice());
                HttpExchangeHelper.json(ex, 200, Json.write(o));
            } else {
                status = 404;
                HttpExchangeHelper.json(ex, 404, error("NOT_FOUND", "No endpoint " + method + " " + path));
            }
        } catch (MatchingException e) {
            status = e.httpStatus();
            LOG.info(() -> e.flowId() + " " + method + " " + path + " → " + e.getMessage());
            HttpExchangeHelper.json(ex, e.httpStatus(), error(e.flowId(), e.getMessage()));
        } catch (IllegalArgumentException e) {
            status = 400;
            HttpExchangeHelper.json(ex, 400, error("DV-05", e.getMessage()));
        } catch (RuntimeException e) {
            status = 500;
            LOG.log(Level.SEVERE, "Unexpected error on " + method + " " + path, e);
            HttpExchangeHelper.json(ex, 500, error("INTERNAL", "Unexpected error, please try again."));
        } finally {
            final int s = status;
            final long ms = (System.nanoTime() - start) / 1_000_000;
            LOG.fine(() -> method + " " + path + " " + s + " " + ms + "ms");
        }
    }

    // ------------------------------------------------------------------------------------------ views

    private List<Object> students() {
        List<Object> out = new ArrayList<>();
        for (Student s : users.findAll()) {
            Map<String, Object> o = profile(s);
            o.put("profileComplete", s.isProfileComplete());
            o.put("matchingEnabled", s.matchingEnabled());
            out.add(o);
        }
        return out;
    }

    private List<Object> criteria() {
        List<Object> out = new ArrayList<>();
        for (MatchCriterion c : MatchCriterion.values()) out.add(Map.of("id", c.name(), "label", c.label()));
        return out;
    }

    private Map<String, Object> findMatches(Map<String, String> q) {
        String studentId = required(q, "studentId");
        MatchCriterion criterion = MatchCriterion.parse(required(q, "criterion"));
        String type = q.get("type");
        MatchFilter filter = MatchFilter.of(q.get("language"),
                type == null || type.isBlank() ? null : StudentType.valueOf(type.toUpperCase()),
                q.get("department"), q.get("availability"));
        List<MatchCandidate> found = matching.findMatches(studentId, criterion, filter);
        List<MatchRequest> mine = matching.requestsOf(studentId);
        List<Object> list = new ArrayList<>();
        for (MatchCandidate c : found) {
            Map<String, Object> o = profile(c.student());
            o.put("score", c.score());
            o.put("reasons", c.reasons());
            o.put("requestStatus", pendingWith(mine, studentId, c.student().id()));
            list.add(o);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("criterion", criterion.name());
        out.put("count", list.size());
        out.put("candidates", list);
        if (list.isEmpty()) out.put("message", "No students match this criterion and these filters. Try removing a filter or another criterion. (UC2-E2)");
        return out;
    }

    private static String pendingWith(List<MatchRequest> mine, String me, String other) {
        for (MatchRequest r : mine) if (r.isBetween(me, other) && r.status() == MatchStatus.PENDING) return "PENDING";
        return null;
    }

    private Map<String, Object> request(MatchRequest r, String viewerId) {
        String otherId = r.requesterId().equals(viewerId) ? r.candidateId() : r.requesterId();
        Student other = matching.requireStudent(otherId);
        Map<String, Object> o = new LinkedHashMap<>();
        o.put("id", r.id());
        o.put("direction", r.requesterId().equals(viewerId) ? "SENT" : "RECEIVED");
        o.put("otherId", other.id());
        o.put("otherName", other.name());
        o.put("criterion", r.criterion().name());
        o.put("score", r.score());
        // Privacy (UC2-A3): a rejected request is shown to the requester as "not accepted", without details.
        o.put("status", r.status());
        o.put("createdAt", r.createdAt().toString());
        if (r.status() == MatchStatus.CONFIRMED) o.put("contact", other.id().toLowerCase() + "@students.etu.edu.tr");
        return o;
    }

    private List<Object> notifications(String studentId) {
        matching.requireStudent(studentId);
        List<Object> out = new ArrayList<>();
        for (Notification n : notifications.findByStudent(studentId)) {
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("id", n.id());
            o.put("message", n.message());
            o.put("requestId", n.requestId());
            o.put("createdAt", n.createdAt().toString());
            out.add(o);
        }
        return out;
    }

    private static Map<String, Object> profile(Student s) {
        Map<String, Object> o = new LinkedHashMap<>();
        o.put("id", s.id());
        o.put("name", s.name());
        o.put("type", s.type());
        o.put("department", s.department());
        o.put("speaks", s.nativeLanguages());
        Map<String, Object> learns = new LinkedHashMap<>();
        s.learningLanguages().forEach((k, v) -> learns.put(k, v.name()));
        o.put("learns", learns);
        o.put("hobbies", s.hobbies());
        o.put("sports", s.sports());
        o.put("availability", s.availability());
        o.put("description", s.description());
        o.put("descriptionLanguage", s.descriptionLanguage());
        return o;
    }

    private static String required(Map<String, String> params, String name) {
        String v = params.get(name);
        if (v == null || v.isBlank()) throw new IllegalArgumentException("Missing parameter '" + name + "'");
        return v.trim();
    }

    private static String error(String code, String message) {
        Map<String, Object> o = new LinkedHashMap<>();
        o.put("error", code);
        o.put("message", message);
        return Json.write(o);
    }
}

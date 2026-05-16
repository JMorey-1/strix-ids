/*
 * Fetches JSON from one of the dashboard API endpoints.
 * If the request fails, an error is thrown and handled by refreshDashboard().
 */
async function fetchJson(url) {
    const response = await fetch(url);

    if (!response.ok) {
        throw new Error(`Request failed: ${url}`);
    }

    return response.json();
}

/*
 * Updates the main status panel with the current IDS mode, engine state,
 * uptime and the model details.
 */
function renderStatus(status) {
    const systemMode = document.getElementById("system-mode");

    systemMode.textContent = status.mode;
    systemMode.className = `badge ${getModeBadgeClass(status.mode)}`;

    document.getElementById("engine-status").textContent = status.engineStatus;
    document.getElementById("uptime").textContent = status.uptime;

    setTextIfPresent("model-algorithm", status.modelAlgorithm);
    setTextIfPresent("model-training-state", status.modelTrainingState);
    setTextIfPresent("model-window-size", status.modelWindowSize);
    setTextIfPresent("model-confidence", status.modelConfidence);
}

/*
 * Safely updates optional dashboard fields.
 * Some elements may not exist if the HTML layout changes later.
 */
function setTextIfPresent(id, value) {
    const element = document.getElementById(id);

    if (element) {
        element.textContent = value;
    }
}

/*
 * Chooses the badge colour for the current IDS operating mode.
 */
function getModeBadgeClass(mode) {
    if (!mode) {
        return "badge-yellow";
    }

    const value = mode.toUpperCase();

    if (value === "MONITORING") {
        return "badge-green";
    }

    if (value === "TRAINING" || value === "WAITING") {
        return "badge-yellow";
    }

    return "badge-red";
}

/*
 * Chooses the badge colour for mitigation states shown in the suspicious IP table.
 */
function getBadgeClass(status) {
    if (!status) {
        return "badge badge-green";
    }

    const value = status.toUpperCase();

    if (value === "BLOCKED") {
        return "badge badge-red";
    }

    if (value === "WATCH" || value === "SUSPICIOUS") {
        return "badge badge-yellow";
    }

    return "badge badge-green";
}

/*
 * Converts a full date-time value into a short time value for feed panels.
 */
function formatTime(dateTime) {
    if (!dateTime) {
        return "-";
    }

    return dateTime.substring(11, 19);
}

/*
 * Displays the IP addresses currently being tracked by the mitigation service.
 * This shows the current state, ie WATCH, SUSPICIOUS or BLOCKED.
 */
function renderSuspiciousIps(items) {
    const tbody = document.getElementById("suspicious-ips-body");

    if (!items.length) {
        tbody.innerHTML = `
            <tr>
                <td colspan="3">No suspicious IP activity</td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = items.map(item => `
        <tr>
            <td>${item.ipAddress}</td>
            <td>${item.suspicionScore}</td>
            <td><span class="${getBadgeClass(item.status)}">${item.status}</span></td>
        </tr>
    `).join("");
}

/*
 * Shows recent WATCH and ALERT events.
 * These are the main detection events produced by the IDS.
 */
function renderAlerts(events) {
    const container = document.getElementById("alerts-feed");

    const alertEvents = events
        .filter(event => event.level === "WATCH" || event.level === "ALERT")
        .slice(0, 8);

    if (!alertEvents.length) {
        container.innerHTML = `
            <div class="feed-item">
                <span class="feed-text">No recent alerts</span>
            </div>
        `;
        return;
    }

    container.innerHTML = alertEvents.map(event => `
        <div class="feed-item">
            <span class="feed-time">${event.time}</span>
            <span class="feed-text">[${event.level}] ${event.message} (${event.ipAddress || "SYSTEM"})</span>
        </div>
    `).join("");
}

/*
 * Shows mitigation actions sent by the IDS to the target application.
 * These are already part of the IDS event stream as SYSTEM messages so this
 * panel does not need a separate backend endpoint.
 */
function renderMitigationActions(events) {
    const container = document.getElementById("mitigation-actions-feed");

    if (!container) {
        return;
    }

    const mitigationEvents = events
        .filter(event => event.message && event.message.includes("[IDS][MITIGATION]"))
        .slice(0, 8);

    if (!mitigationEvents.length) {
        container.innerHTML = `
            <div class="feed-item">
                <span class="feed-text">No mitigation actions yet</span>
            </div>
        `;
        return;
    }

    container.innerHTML = mitigationEvents.map(event => `
        <div class="feed-item">
            <span class="feed-time">${event.time}</span>
            <span class="feed-text">${event.ipAddress || "SYSTEM"}: ${event.message}</span>
        </div>
    `).join("");
}

/*
 * Converts the event level into a CSS class for the live IDS console.
 */
function getConsoleLevelClass(level) {
    if (!level) {
        return "waiting";
    }

    return level.toLowerCase();
}

/*
 * Formats anomaly scores so the console stays readable.
 */
function formatScore(score) {
    if (score === null || score === undefined) {
        return "-";
    }

    return Number(score).toFixed(3);
}

/*
 * Renders the live IDS console.
 * This gives the dashboard a terminal-like view of recent scoring, alerts,
 * mitigation messages, etc.
 */
function renderIdsEvents(events) {
    const container = document.getElementById("ids-console-feed");

    if (!container) {
        return;
    }

    if (!events.length) {
        container.innerHTML = `
            <div class="console-line">
                <span class="console-message">Waiting for IDS events...</span>
            </div>
        `;
        return;
    }

    const visibleEvents = events.slice(0, 40);

    container.innerHTML = visibleEvents.map(event => `
        <div class="console-line">
            <span class="console-time">${event.time}</span>
            <span class="console-level ${getConsoleLevelClass(event.level)}">${event.level}</span>
            <span class="console-ip">${event.ipAddress || "SYSTEM"}</span>
            <span class="console-score">${formatScore(event.score)}</span>
            <span class="console-message">${event.message}</span>
        </div>
    `).join("");
}

/*
 * Updates the simple request summary counters.
 * SCORE means normal scoring activity while WATCH and ALERT are flagged events.
 */
function renderRequestSummary(events) {
    const normalCount = events.filter(event => event.level === "SCORE").length;

    const flaggedCount = events.filter(event =>
        event.level === "WATCH" || event.level === "ALERT"
    ).length;

    const alertCount = events.filter(event => event.level === "ALERT").length;

    document.getElementById("normal-request-count").textContent = normalCount;
    document.getElementById("flagged-request-count").textContent = flaggedCount;
    document.getElementById("alert-event-count").textContent = alertCount;
}

/*
 * Updates the large hero status area at the top of the dashboard.
 * This makes the current system state obvious during my demos.
 */
function renderHeroState(status) {
    const heroStatus = document.getElementById("hero-status");
    const statusText = document.getElementById("hero-status-text");
    const description = document.getElementById("hero-description");

    if (!heroStatus || !statusText || !description) {
        return;
    }

    heroStatus.className = "hero-status";

    if (status.mode === "TRAINING") {
        heroStatus.classList.add("training");
        statusText.textContent = "Collecting training traffic";
        description.textContent = "Strix is currently collecting normal request behaviour so the anomaly model can learn a baseline.";
        return;
    }

    if (status.mode === "MONITORING") {
        heroStatus.classList.add("monitoring");
        statusText.textContent = "Monitoring live traffic";
        description.textContent = "Strix is trained and evaluating incoming request behaviour against the learned baseline.";
        return;
    }

    heroStatus.classList.add("waiting");
    statusText.textContent = "Waiting for model training";
    description.textContent = "Strix has started, but the anomaly model has not been trained yet. Run the warm-up and training phase before starting detection traffic.";
}

/*
 * Main dashboard refresh function.
 * It fetches the current status, suspicious IP records and recent IDS events
 * in parallel then updates each of my panels.
 */
async function refreshDashboard() {
    try {
        /*
         * Mitigation actions are now pulled from the IDS event stream, so the old
         * /api/blacklist request is no longer needed for this dashboard view.
         */
        const [status, suspiciousIps, idsEvents] = await Promise.all([
            fetchJson("/api/status"),
            fetchJson("/api/suspicious-ips"),
            fetchJson("/api/ids-events")
        ]);

        renderStatus(status);
        renderHeroState(status);
        renderAlerts(idsEvents);
        renderSuspiciousIps(suspiciousIps);
        renderMitigationActions(idsEvents);
        renderIdsEvents(idsEvents);
        renderRequestSummary(idsEvents);
    } catch (error) {
        console.error("Dashboard refresh failed:", error);
    }
}

/*
 * Load the dashboard immediately and then refresh it once per second so the UI
 * stays live while the traffic generator is running.
 */
refreshDashboard();
setInterval(refreshDashboard, 1000);
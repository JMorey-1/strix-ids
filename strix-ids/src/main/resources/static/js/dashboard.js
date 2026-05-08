async function fetchJson(url) {
    // Fetch JSON data from one backend endpoint.
    const response = await fetch(url);

    // Stop the refresh if the endpoint returns an error.
    if (!response.ok) {
        throw new Error(`Request failed: ${url}`);
    }

    return response.json();
}

function renderStatus(status) {
    // Update the sidebar system status values.
    const systemMode = document.getElementById("system-mode");

    systemMode.textContent = status.mode;
    systemMode.className = `badge ${getModeBadgeClass(status.mode)}`;

    document.getElementById("engine-status").textContent = status.engineStatus;
    document.getElementById("uptime").textContent = status.uptime;

    // Update the model status panel if the elements exist.
    setTextIfPresent("model-algorithm", status.modelAlgorithm);
    setTextIfPresent("model-training-state", status.modelTrainingState);
    setTextIfPresent("model-window-size", status.modelWindowSize);
    setTextIfPresent("model-confidence", status.modelConfidence);
}

function setTextIfPresent(id, value) {
    // Small helper to avoid errors if a dashboard element is missing.
    const element = document.getElementById(id);

    if (element) {
        element.textContent = value;
    }
}

function getModeBadgeClass(mode) {
    // Choose the badge colour used for the current IDS mode.
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

function getBadgeClass(status) {
    // Choose the badge colour used for mitigation statuses.
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

function formatTime(dateTime) {
    // Convert a full date-time string into just HH:mm:ss.
    if (!dateTime) {
        return "-";
    }

    return dateTime.substring(11, 19);
}

function renderSuspiciousIps(items) {
    // Fill the suspicious IP table from mitigation records.
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

function renderAlerts(events) {
    // Show only WATCH and ALERT events in the alert feed.
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

function renderBlacklist(items) {
    // Fill the blacklist panel from blocked mitigation records.
    const container = document.getElementById("blacklist-feed");

    if (!items.length) {
        container.innerHTML = `
            <div class="feed-item">
                <span class="feed-text">Blacklist is empty</span>
            </div>
        `;
        return;
    }

    container.innerHTML = items.map(item => `
        <div class="feed-item">
            <span class="feed-time">${formatTime(item.blockedAt)}</span>
            <span class="feed-text">${item.ipAddress} blocked: ${item.reason}</span>
        </div>
    `).join("");
}

function getConsoleLevelClass(level) {
    // Map IDS event levels to CSS classes.
    if (!level) {
        return "waiting";
    }

    return level.toLowerCase();
}

function formatScore(score) {
    // Keep anomaly scores readable in the console.
    if (score === null || score === undefined) {
        return "-";
    }

    return Number(score).toFixed(3);
}

function renderIdsEvents(events) {
    // Render the live IDS console from recent IDS log entries.
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

    // Limit the visible console list so the panel stays readable.
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

function renderRequestSummary(events) {
    // Count normal, flagged and alert-level events for the summary cards.
    const normalCount = events.filter(event => event.level === "SCORE").length;

    const flaggedCount = events.filter(event =>
        event.level === "WATCH" || event.level === "ALERT"
    ).length;

    const alertCount = events.filter(event => event.level === "ALERT").length;

    document.getElementById("normal-request-count").textContent = normalCount;
    document.getElementById("flagged-request-count").textContent = flaggedCount;
    document.getElementById("alert-event-count").textContent = alertCount;
}

function renderHeroState(status) {
    // Update the main detection state panel based on the IDS mode.
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

    // Default state before collection or training has happened.
    heroStatus.classList.add("waiting");
    statusText.textContent = "Waiting for model training";
    description.textContent = "Strix has started, but the anomaly model has not been trained yet. Run the warm-up and training phase before starting detection traffic.";
}

async function refreshDashboard() {
    try {
        // Load all dashboard data together so the UI refreshes as one snapshot.
        const [status, suspiciousIps, blacklist, idsEvents] = await Promise.all([
            fetchJson("/api/status"),
            fetchJson("/api/suspicious-ips"),
            fetchJson("/api/blacklist"),
            fetchJson("/api/ids-events")
        ]);

        // Update each dashboard section with the latest backend data.
        renderStatus(status);
        renderHeroState(status);
        renderAlerts(idsEvents);
        renderSuspiciousIps(suspiciousIps);
        renderBlacklist(blacklist);
        renderIdsEvents(idsEvents);
        renderRequestSummary(idsEvents);
    } catch (error) {
        // Keep the dashboard running even if one refresh fails.
        console.error("Dashboard refresh failed:", error);
    }
}

// Load the dashboard once, then refresh it every second.
refreshDashboard();
setInterval(refreshDashboard, 1000);
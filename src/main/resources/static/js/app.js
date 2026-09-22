(() => {
    "use strict";

    const bodyInput = document.getElementById("body");
    const urlInput = document.getElementById("url");
    const bodyCount = document.getElementById("body-count");
    const bodyCounter = document.getElementById("body-counter");

    if (bodyInput && urlInput && bodyCount && bodyCounter) {
        const updateBodyCount = () => {
            const body = bodyInput.value.trim() ? bodyInput.value : "";
            const url = urlInput.value.trim() ? urlInput.value : "";
            const separator = body && url ? "\n\n" : "";
            const count = Array.from(`${body}${separator}${url}`).length;
            bodyCount.textContent = String(count);
            bodyCounter.classList.toggle("is-over-limit", count > 500);
        };
        bodyInput.addEventListener("input", updateBodyCount);
        urlInput.addEventListener("input", updateBodyCount);
        updateBodyCount();
    }

    const rows = Array.from(document.querySelectorAll(".post-row"));
    const searchFilter = document.getElementById("search-filter");
    const channelFilter = document.getElementById("channel-filter");
    const statusFilter = document.getElementById("status-filter");
    const postCount = document.getElementById("post-count");
    const filterSummary = document.getElementById("filter-summary");
    const emptyState = document.getElementById("empty-state");

    const formatDateTime = (value) => {
        if (!value) return "-";
        const parts = value.replace(" ", "T").split("T");
        return parts.length < 2 ? value : `${parts[0].replaceAll("-", "/")} ${parts[1].slice(0, 5)}`;
    };

    document.querySelectorAll(".date-cell").forEach((cell) => {
        cell.textContent = formatDateTime(cell.dataset.value || cell.textContent.trim());
    });
    document.querySelectorAll(".channel-cell").forEach((cell) => {
        if (cell.textContent.trim() === "THREADS") cell.textContent = "Threads";
    });

    const statusCounts = rows.reduce((counts, row) => {
        const status = row.dataset.status;
        counts[status] = (counts[status] || 0) + 1;
        return counts;
    }, {});
    const setKpi = (id, status) => {
        const element = document.getElementById(id);
        if (element) element.textContent = `${statusCounts[status] || 0} 件`;
    };
    setKpi("kpi-scheduled", "PENDING");
    setKpi("kpi-posted", "POSTED");
    setKpi("kpi-error", "ERROR");

    if (!searchFilter || !channelFilter || !statusFilter || !postCount || !filterSummary || !emptyState) return;
    const selectedText = (select) => select.options[select.selectedIndex].text;
    const applyFilters = () => {
        const query = searchFilter.value.trim().toLocaleLowerCase("ja");
        const selectedChannel = channelFilter.value;
        const selectedStatus = statusFilter.value;
        let visibleCount = 0;

        rows.forEach((row) => {
            const searchableText = `${row.dataset.title || ""} ${row.dataset.body || ""}`.toLocaleLowerCase("ja");
            const channels = (row.dataset.channel || "").split(",").map((value) => value.trim());
            const visible = (!query || searchableText.includes(query))
                && (selectedChannel === "all" || channels.includes(selectedChannel))
                && (selectedStatus === "all" || row.dataset.status === selectedStatus);
            row.hidden = !visible;
            if (visible) visibleCount += 1;
        });

        postCount.textContent = `全 ${visibleCount} 件`;
        emptyState.hidden = visibleCount !== 0;
        const summary = [];
        if (query) summary.push(`検索: ${searchFilter.value.trim()}`);
        summary.push(`チャネル: ${selectedText(channelFilter)}`);
        summary.push(`状態: ${selectedText(statusFilter)}`);
        filterSummary.textContent = `対象：${summary.join(" / ")}`;
    };

    searchFilter.addEventListener("input", applyFilters);
    channelFilter.addEventListener("change", applyFilters);
    statusFilter.addEventListener("change", applyFilters);
    applyFilters();
})();

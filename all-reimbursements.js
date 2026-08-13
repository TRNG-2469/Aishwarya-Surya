const storedUser = localStorage.getItem("currentUser");

if (!storedUser) {
    window.location.href = "/";
}

const currentUser = JSON.parse(storedUser);

document.getElementById("welcomeMsg").textContent =
    "Welcome, " + currentUser.username;

document.getElementById("backBtn")
    .addEventListener("click", function () {
        window.location.href = "/reimbursements.html";
    });

async function loadAllReimbursements(status = "", department = "") {
    try {
        let url = "/api/reimbursements";

        if (status || department) {
            const params = new URLSearchParams();

            if (status) {
                params.append("status", status);
            }

            if (department) {
                params.append("department", department);
            }

            url = `/api/reimbursements/filter?${params.toString()}`;
        }

        const response = await fetch(url, {
            method: "GET",
            credentials: "include"
        });

        const reimbursementList =
            document.getElementById("reimbursementList");

        if (response.status === 401) {
            localStorage.removeItem("currentUser");
            window.location.href = "/";
            return;
        }

        if (response.status === 403) {
            reimbursementList.textContent =
                "Access denied. Only managers can view all reimbursements.";
            return;
        }

        if (!response.ok) {
            const errorText = await response.text();

            reimbursementList.textContent =
                errorText || "Unable to load reimbursements.";

            return;
        }

        const reimbursements = await response.json();

        reimbursementList.innerHTML = "";

        if (reimbursements.length === 0) {
            reimbursementList.textContent =
                "No reimbursements found.";
            return;
        }

        reimbursements.forEach(function (r) {
            const item = document.createElement("p");

            item.textContent =
                "User #" + r.userId +
                " | $" + r.amount +
                " | " +
                r.description +
                " | " +
                r.type +
                " | " +
                r.status +
                (r.resolverId
                    ? " | Resolved by #" + r.resolverId
                    : "");

            reimbursementList.appendChild(item);
        });

    } catch (err) {
        console.error("Error loading reimbursements:", err);

        document.getElementById("reimbursementList").textContent =
            "Unable to connect to the server.";
    }
}
document.getElementById("filterBtn")
    .addEventListener("click", function () {

        const status =
            document.getElementById("statusFilter").value;

        const department =
            document.getElementById("departmentFilter").value.trim();

        loadAllReimbursements(status, department);
    });

document.getElementById("clearFilterBtn")
    .addEventListener("click", function () {

        document.getElementById("statusFilter").value = "";
        document.getElementById("departmentFilter").value = "";

        loadAllReimbursements();
    });

document.getElementById("logoutBtn")
    .addEventListener("click", async function () {

        try {
            const response = await fetch(
                "/logout",
                {
                    method: "POST",
                    credentials: "include"
                }
            );

            if (response.ok) {
                localStorage.removeItem("currentUser");
                window.location.href = "/";
            }

        } catch (err) {
            console.error("Logout error:", err);
        }
    });

loadAllReimbursements();
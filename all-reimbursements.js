const storedUser = localStorage.getItem("currentUser");

if (!storedUser) {
    window.location.href = "/";
}

const currentUser = JSON.parse(storedUser);

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

        const table = document.createElement("table");

        const thead = document.createElement("thead");
        const headerRow = document.createElement("tr");

        [
            "User",
            "Amount",
            "Description",
            "Type",
            "Status",
            "Resolved By",
            "Action"
        ].forEach(function (headerText) {
            const th = document.createElement("th");
            th.textContent = headerText;
            headerRow.appendChild(th);
        });

        thead.appendChild(headerRow);
        table.appendChild(thead);

        const tbody = document.createElement("tbody");

        reimbursements.forEach(function (r) {
            const row = document.createElement("tr");

            const userCell = document.createElement("td");
            userCell.textContent = "#" + r.userId;
            row.appendChild(userCell);

            const amountCell = document.createElement("td");
            amountCell.textContent = "$" + r.amount;
            row.appendChild(amountCell);

            const descriptionCell = document.createElement("td");
            descriptionCell.textContent = r.description;
            row.appendChild(descriptionCell);

            const typeCell = document.createElement("td");
            typeCell.textContent = r.type;
            row.appendChild(typeCell);

            const statusCell = document.createElement("td");
            statusCell.textContent = r.status;
            row.appendChild(statusCell);

            const resolvedCell = document.createElement("td");
            resolvedCell.textContent =
                r.resolverId ? "#" + r.resolverId : "-";
            row.appendChild(resolvedCell);

            const actionCell = document.createElement("td");

            if (r.status === "PENDING") {
                const approveBtn = document.createElement("button");
                approveBtn.textContent = "Approve";

                approveBtn.addEventListener("click", async function () {
                    try {
                        const response = await fetch(
                            `/api/reimbursements/${r.id}/approve`,
                            {
                                method: "PUT",
                                credentials: "include"
                            }
                        );

                        if (response.ok) {
                            alert("Reimbursement approved.");

                            const status =
                                document.getElementById("statusFilter").value;

                            const department =
                                document.getElementById("departmentFilter").value.trim();

                            loadAllReimbursements(status, department);
                        } else {
                            const errorText = await response.text();
                            alert(errorText || "Unable to approve reimbursement.");
                        }

                    } catch (err) {
                        console.error("Approve error:", err);
                        alert("Unable to connect to the server.");
                    }
                });

                const denyBtn = document.createElement("button");
                denyBtn.textContent = "Deny";

                denyBtn.addEventListener("click", async function () {
                    try {
                        const response = await fetch(
                            `/api/reimbursements/${r.id}/deny`,
                            {
                                method: "PUT",
                                credentials: "include"
                            }
                        );

                        if (response.ok) {
                            alert("Reimbursement denied.");

                            const status =
                                document.getElementById("statusFilter").value;

                            const department =
                                document.getElementById("departmentFilter").value.trim();

                            loadAllReimbursements(status, department);
                        } else {
                            const errorText = await response.text();
                            alert(errorText || "Unable to deny reimbursement.");
                        }

                    } catch (err) {
                        console.error("Deny error:", err);
                        alert("Unable to connect to the server.");
                    }
                });

                actionCell.appendChild(approveBtn);
                actionCell.appendChild(denyBtn);
            }

            row.appendChild(actionCell);
            tbody.appendChild(row);
        });

        table.appendChild(tbody);
        reimbursementList.appendChild(table);

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
                "/api/auth/logout",
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
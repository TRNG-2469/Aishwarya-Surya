const storedUser = localStorage.getItem("currentUser");

if (!storedUser) {
    window.location.href = "/";
}

const currentUser = JSON.parse(storedUser);

document.getElementById("welcomeMsg").textContent =
    "Welcome, " + currentUser.username;


// Manager button
if (currentUser.role === "MANAGER") {
    const managerViewBtn = document.getElementById("managerViewBtn");

    if (managerViewBtn) {
        managerViewBtn.style.display = "inline-block";

        managerViewBtn.addEventListener("click", function () {
            window.location.href = "/all-reimbursements.html";
        });
    }
}


// Load reimbursements
async function loadReimbursements(status = "ALL") {
    try {
        let url = `/api/reimbursements/${currentUser.id}`;

        if (status !== "ALL") {
            url += `/status/${status}`;
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

        ["Amount", "Description", "Type", "Status", "Action"].forEach(
            function (headerText) {
                const th = document.createElement("th");
                th.textContent = headerText;
                headerRow.appendChild(th);
            }
        );

        thead.appendChild(headerRow);
        table.appendChild(thead);

        const tbody = document.createElement("tbody");

        reimbursements.forEach(function (r) {
            const row = document.createElement("tr");

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

            const actionCell = document.createElement("td");

            if (r.status === "PENDING") {
                const editBtn = document.createElement("button");
                editBtn.textContent = "Edit";

                let isEditing = false;

                editBtn.addEventListener("click", async function () {

                    // --- Enter edit mode: swap cells for inputs ---
                    if (!isEditing) {
                        isEditing = true;

                        const amountInput = document.createElement("input");
                        amountInput.type = "number";
                        amountInput.step = "0.01";
                        amountInput.value = r.amount;

                        const descriptionInput = document.createElement("input");
                        descriptionInput.type = "text";
                        descriptionInput.value = r.description;

                        const typeInput = document.createElement("select");

                        ["TRAVEL", "MEALS", "LODGING", "OTHER"].forEach(function (optionValue) {
                            const option = document.createElement("option");
                            option.value = optionValue;
                            option.textContent = optionValue;

                            if (optionValue === r.type) {
                                option.selected = true;
                            }

                            typeInput.appendChild(option);
                        });

                        amountCell.textContent = "";
                        amountCell.appendChild(amountInput);

                        descriptionCell.textContent = "";
                        descriptionCell.appendChild(descriptionInput);

                        typeCell.textContent = "";
                        typeCell.appendChild(typeInput);

                        editBtn.textContent = "Submit";
                        return;
                    }

                    // --- Submit mode: read inputs and send PUT ---
                    const amountInput = amountCell.querySelector("input");
                    const descriptionInput = descriptionCell.querySelector("input");
                    const typeInput = typeCell.querySelector("select");

                    const newAmount = amountInput.value;
                    const newDescription = descriptionInput.value;
                    const newType = typeInput.value;

                    try {
                        const response = await fetch(
                            `/api/reimbursements/${r.id}`,
                            {
                                method: "PUT",
                                headers: {
                                    "Content-Type": "application/json"
                                },
                                credentials: "include",
                                body: JSON.stringify({
                                    amount: parseFloat(newAmount),
                                    description: newDescription,
                                    type: newType.toUpperCase()
                                })
                            }
                        );

                        if (response.ok) {
                            alert("Reimbursement updated successfully.");

                            const selectedStatus =
                                document.getElementById("statusFilter").value;

                            loadReimbursements(selectedStatus);
                        } else {
                            const errorText = await response.text();
                            alert(errorText || "Unable to update reimbursement.");
                        }

                    } catch (err) {
                        console.error("Update reimbursement error:", err);
                        alert("Unable to connect to the server.");
                    }
                });

                actionCell.appendChild(editBtn);
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


// Submit reimbursement
document.getElementById("reimbursementForm")
    .addEventListener("submit", async function (e) {

        e.preventDefault();

        const amount =
            parseFloat(document.getElementById("amount").value);

        const description =
            document.getElementById("description").value;

        const type =
            document.getElementById("type").value;

        const reimbursementMsg =
            document.getElementById("reimbursementMsg");

        reimbursementMsg.textContent = "";

        try {
            const response = await fetch("/api/reimbursements", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                credentials: "include",
                body: JSON.stringify({
                    amount: amount,
                    description: description,
                    type: type
                })
            });

            if (response.ok) {
                const reimbursement = await response.json();

                reimbursementMsg.textContent =
                    "Reimbursement submitted successfully. Status: " +
                    reimbursement.status;

                document.getElementById("reimbursementForm").reset();

                const selectedStatus =
                    document.getElementById("statusFilter").value;

                loadReimbursements(selectedStatus);

            } else {
                const errorText = await response.text();

                reimbursementMsg.textContent =
                    errorText || "Unable to submit reimbursement.";
            }

        } catch (err) {
            console.error("Reimbursement submission error:", err);

            reimbursementMsg.textContent =
                "Unable to connect to the server.";
        }
    });


// Logout
document.getElementById("logoutBtn")
    .addEventListener("click", async function () {

        try {
            const response = await fetch("/api/auth/logout", {
                method: "POST",
                credentials: "include"
            });

            if (response.ok) {
                localStorage.removeItem("currentUser");
                window.location.href = "/";
            } else {
                console.error("Logout failed:", response.status);
            }

        } catch (err) {
            console.error("Logout error:", err);
        }
    });


// Status filter
document.getElementById("statusFilter")
    .addEventListener("change", function () {
        loadReimbursements(this.value);
    });


// Initial load
loadReimbursements();
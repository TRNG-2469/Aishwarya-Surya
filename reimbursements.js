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

        reimbursements.forEach(function (r) {
            const item = document.createElement("p");

            item.textContent =
                "$" + r.amount +
                " | " +
                r.description +
                " | " +
                r.type +
                " | " +
                r.status;

            reimbursementList.appendChild(item);
        });

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
            const response = await fetch("/logout", {
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
let isRegisterMode = false;

document.getElementById("toggleRegister").addEventListener("click", function (e) {
    e.preventDefault();

    isRegisterMode = !isRegisterMode;

    const registerFields = document.getElementById("registerFields");
    const formTitle = document.getElementById("formTitle");
    const submitBtn = document.getElementById("submitBtn");

    if (isRegisterMode) {
        registerFields.style.display = "block";
        formTitle.textContent = "Register";
        submitBtn.textContent = "Register";
        this.textContent = "Already have an account? Log in here";
    } else {
        registerFields.style.display = "none";
        formTitle.textContent = "Login";
        submitBtn.textContent = "Login";
        this.textContent = "Don't have an account? Register here";
    }
});

document.getElementById("authForm").addEventListener("submit", async function (e) {
    e.preventDefault();

    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;
    const msg = document.getElementById("msg");

    msg.textContent = "";

    if (isRegisterMode) {
        const department = document.getElementById("department").value;

        try {
            const response = await fetch("/", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify({
                    action: "register",
                    username: username,
                    password: password,
                    department: department
                })
            });

            if (response.ok) {
                const newUser = await response.json();
                msg.textContent = "Account created successfully for " + newUser.username + "! You can now log in.";
                localStorage.setItem("currentUser", JSON.stringify(newUser));

                document.getElementById("toggleRegister").click();
            } else {
                msg.textContent = "User already has an account.";
            }
        } catch (err) {
            console.error("Registration network error:", err);
            msg.textContent = "Unable to connect to the server.";
        }

    } else {
        try {
            const response = await fetch("/", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify({
                    action: "login",
                    username: username,
                    password: password
                })
            });

            if (response.ok) {
                const user = await response.json();
                msg.textContent = "Welcome back, " + user.username + "! Redirecting...";
                localStorage.setItem("currentUser", JSON.stringify(user));

                setTimeout(() => {
                    window.location.href = `/api/reimbursements/${user.id}`;
                }, 1000);

            } else if (response.status === 401) {
                msg.textContent = "Incorrect username or password.";
            } else {
                const errorText = await response.text();
                msg.textContent = errorText || ("Server error during login (" + response.status + ").");
            }
        } catch (err) {
            console.error("Login network error:", err);
            msg.textContent = "Unable to connect to the server.";
        }
    }
});

async function fetchUserReimbursements(userId) {
    try {
        const response = await fetch(`/api/reimbursements/${userId}`, {
            method: "GET",
            credentials: "include"
        });

        if (response.status === 403) {
            alert("Access Denied: You are not authorized to view these reimbursements.");
            return null;
        }

        if (!response.ok) {
            throw new Error(`Failed to fetch reimbursements: ${response.status}`);
        }

        return await response.json();
    } catch (err) {
        console.error("Error loading reimbursements:", err);
    }
}
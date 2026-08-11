let isRegisterMode = false;

// Toggle between Login and Register modes
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

// Form Submission Handler
document.getElementById("authForm").addEventListener("submit", async function (e) {
    e.preventDefault();

    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;
    const msg = document.getElementById("msg");

    msg.textContent = "";

    if (isRegisterMode) {
        // --- REGISTER ---
        const department = document.getElementById("department").value;
        const role = document.getElementById("role").value;

        // POST request sent directly to root URL "/"
        const response = await fetch("/", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                action: "register", // Helps Java differentiate if needed
                username: username,
                password: password,
                department: department,
                role: role
            })
        });

        if (response.ok) {
            const newUser = await response.json();
            msg.textContent = "Account created successfully for " + newUser.username + "!";
            localStorage.setItem("currentUser", JSON.stringify(newUser));
        } else {
            msg.textContent = "Registration failed. Username may already be taken.";
        }

    } else {
        // --- LOGIN ---
        // POST request sent directly to root URL "/"
        const response = await fetch("/", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                action: "login", // Helps Java differentiate if needed
                username: username,
                password: password
            })
        });

        if (response.ok) {
            const user = await response.json();
            msg.textContent = "Welcome back, " + user.username + "!";
            localStorage.setItem("currentUser", JSON.stringify(user));
        } else if (response.status === 401) {
            msg.textContent = "Incorrect username or password.";
        } else {
            msg.textContent = "Server error during login (" + response.status + ").";
        }
    }
});
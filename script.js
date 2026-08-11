document.getElementById("authForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;
    const msg = document.getElementById("msg");

    const res = await fetch("/api/users");
    const users = await res.json();
    const user = users.find(u => u.username === username);

    if (user) {
        msg.textContent = `Welcome back, ${user.username}! Role: ${user.role}`;
    } else {
        const reg = await fetch("/api/users/register", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password, role: "EMPLOYEE" })
        });
        msg.textContent = reg.ok ? "Account created as Employee!" : "Registration failed.";
    }
});
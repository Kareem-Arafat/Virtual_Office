async function triggerLogin()
{
    const username = document.getElementById("loginUsername").value.trim();
    const staffId = document.getElementById("loginStaffId").value.trim();
    const password = document.getElementById("loginPassword").value;

    const response = await fetch("/auth/login",
    {
        method: "POST",

        headers:
        {
            "Content-Type": "application/json"
        },

        body: JSON.stringify(
        {
            username: username,
            staffId: staffId,
            password: password
        })
    });

    if(response.ok)
    {
        const data = await response.json();
        sessionStorage.setItem("token", data.token);
        sessionStorage.setItem("user", JSON.stringify(data.user));

        window.location.href = "tasks.html";
    }
    else
    {
        const error = await response.text();
        alert(error || "Login failed");
    }
}








async function triggerRegister()
{
    const username = document.getElementById("regName").value;
    const email = document.getElementById("regEmail").value;
    const password = document.getElementById("regPass").value;

    const response = await fetch("/auth/register",
    {
        method: "POST",

        headers:
        {
            "Content-Type": "application/json"
        },

        body: JSON.stringify(
        {
            username: username,
            email: email,
            password: password
        })
    });

    if (response.ok)
    {
        window.location.href = "login.html";
    }
    else
    {
        const error = await response.text();
        alert(error || "Registration failed");
    }
}




// ==================== PASSWORD EYE ====================

function togglePassword(inputId, eyeId)
{
    const passwordInput = document.getElementById(inputId);
    const eyeIcon = document.getElementById(eyeId);

    if(passwordInput.type === "password")
    {
        passwordInput.type = "text";

        eyeIcon.classList.remove("fa-eye");
        eyeIcon.classList.add("fa-eye-slash");
    }
    else
    {
        passwordInput.type = "password";

        eyeIcon.classList.remove("fa-eye-slash");
        eyeIcon.classList.add("fa-eye");
    }
}

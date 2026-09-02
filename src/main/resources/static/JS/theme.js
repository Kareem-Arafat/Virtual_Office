// ==================== DARK MODE ====================

let initialTheme = "light";

if(localStorage.getItem("theme") === "dark")
{
    initialTheme = "dark";
}


document.documentElement.setAttribute("data-theme", initialTheme);


document.addEventListener("DOMContentLoaded", function ()
{
    const darkToggle = document.getElementById("darkToggle");

    const savedTheme = initialTheme;

    if(savedTheme === "dark")
    {
        document.documentElement.setAttribute("data-theme", "dark");

        if(darkToggle)
        {
            darkToggle.checked = true;
        }
    }
    else
    {
        document.documentElement.setAttribute("data-theme", "light");

        if(darkToggle)
        {
            darkToggle.checked = false;
        }
    }


    if(darkToggle)
    {
        darkToggle.addEventListener("change", function ()
        {
            if(darkToggle.checked)
            {
                document.documentElement.setAttribute("data-theme", "dark");

                localStorage.setItem("theme", "dark");
            }
            else
            {
                document.documentElement.setAttribute("data-theme", "light");

                localStorage.setItem("theme", "light");
            }
        });
    }
});







function updateAuthNavbar()
{
    const token = sessionStorage.getItem("token");
    const loginButton = document.querySelector(".login_button");
    const signupButton = document.querySelector(".signup_button");


    if(token)
    {
        if(loginButton)
        {
            loginButton.style.display = "none";
        }

        if(signupButton)
        {
            signupButton.style.display = "none";
        }
    }
    else
    {
        if(loginButton)
        {
            loginButton.style.display = "";
        }

        if(signupButton)
        {
            signupButton.style.display = "";
        }
    }
}

document.addEventListener("DOMContentLoaded", updateAuthNavbar);









function loadNavbarUser()
{
    const savedUser = sessionStorage.getItem("user");

    const nameElement = document.getElementById("navUserName");

    if(!nameElement)
    {
        return;
    }

    if(!savedUser)
    {
        nameElement.textContent = "Guest";
        return;
    }

    const user = JSON.parse(savedUser);

    nameElement.textContent = user.username || user.name || "Guest";
}



document.addEventListener("DOMContentLoaded", loadNavbarUser);




async function submitChangePassword()
{
    const currentPassword = document.getElementById("currentPassword");
    const newPassword = document.getElementById("newPassword");
    const confirmPassword = document.getElementById("confirmNewPassword");
    const status = document.getElementById("changePasswordStatus");

    if(!currentPassword || !newPassword || !confirmPassword)
    {
        return;
    }
    if(newPassword.value !== confirmPassword.value)
    {
        status.textContent = "New passwords do not match.";
        return;
    }

    const response = await fetch("http://localhost:8080/api/users/change-password",
    {
        method: "PUT",
        headers:
        {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + sessionStorage.getItem("token")
        },
        body: JSON.stringify({oldPassword: currentPassword.value, newPassword: newPassword.value})
    });

    
   if(response.ok)
    {
        status.textContent = "Password changed successfully.";
    }
    else
    {
        const message = await response.text();

        if(message)
        {
            status.textContent = message;
        }
        else
        {
            status.textContent = "Password could not be changed.";
        }
    }


    if(response.ok)
    {
        currentPassword.value = "";
        newPassword.value = "";
        confirmPassword.value = "";
    }
}

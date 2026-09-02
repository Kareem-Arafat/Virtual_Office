let passwordResetEmail = "";
let verifiedResetCode = "";




async function requestPasswordResetCode()
{
    const email = document.getElementById("resetEmail").value.trim();
    const status = document.getElementById("resetStatus");


    if(!email)
    {
        status.textContent = "Please enter your email.";
        return;
    }


    const response = await fetch
    (
        "http://localhost:8080/auth/forgot-password/request",
        {
            method: "POST",

            headers:
            {
                "Content-Type":"application/json"
            },

            body: JSON.stringify(
            {
                email: email
            })
        }
    );


    if(!response.ok)
    {
        status.textContent = "Unable to send reset code.";
        return;
    }


    passwordResetEmail = email;


    document.getElementById("resetDeliveryTarget").textContent = email;

    showResetStep("verify");

    status.textContent = "Reset code sent.";
}






async function verifyPasswordResetCode()
{
    const code = document.getElementById("resetOtpCode").value.trim();
    const status = document.getElementById("resetStatus");


    if(code.length !== 5)
    {
        status.textContent = "Enter the 5-digit reset code.";
        return;
    }


    const response = await fetch
    (
        "http://localhost:8080/auth/forgot-password/verify",
        {
            method: "POST",

            headers:
            {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(
            {
                email: passwordResetEmail,
                code: code
            })
        }
    );


    if(!response.ok)
    {
        status.textContent = "Invalid or expired code.";
        return;
    }


    verifiedResetCode = code;

    showResetStep("password");

    status.textContent = "Code verified.";
}







async function submitForgotPasswordReset()
{
    const newPassword = document.getElementById("resetNewPassword").value;
    const confirmPassword = document.getElementById("resetConfirmPassword").value;
    const status = document.getElementById("resetStatus");

    if(!newPassword)
    {
        status.textContent = "Enter a new password.";
        return;
    }


    if(newPassword !== confirmPassword)
    {
        status.textContent = "Passwords do not match.";
        return;
    }


    const response = await fetch
    (
        "http://localhost:8080/auth/forgot-password/reset",
        {
            method: "POST",

            headers:
            {
                "Content-Type": "application/json"
            },

            body: JSON.stringify(
            {
                email: passwordResetEmail,
                code: verifiedResetCode,
                newPassword: newPassword
            })
        }
    );


    if(!response.ok)
    {
        status.textContent = "Password reset failed.";
        return;
    }

    status.textContent = "Password updated successfully.";

    setTimeout(function()
    {
        window.location.href = "login.html"; 
    }, 
    1200);
}






function showResetStep(step)
{
    const requestPanel = document.getElementById("resetStepRequest");
    const verifyPanel = document.getElementById("resetStepVerify");
    const passwordPanel = document.getElementById("resetStepPassword");


    requestPanel.classList.add("auth-panel-hidden");

    verifyPanel.classList.add("auth-panel-hidden");

    passwordPanel.classList.add("auth-panel-hidden");


    document.getElementById("resetStepRequestBadge").classList.remove("is-active");
    document.getElementById("resetStepVerifyBadge").classList.remove("is-active");
    document.getElementById("resetStepPasswordBadge").classList.remove("is-active");


    if(step === "request")
    {
        requestPanel.classList.remove("auth-panel-hidden");
        document.getElementById("resetStepRequestBadge").classList.add("is-active");
    }


    if(step === "verify")
    {
        verifyPanel.classList.remove("auth-panel-hidden");
        document.getElementById("resetStepVerifyBadge").classList.add("is-active");
    }


    if(step === "password")
    {
        passwordPanel.classList.remove("auth-panel-hidden");
        document.getElementById("resetStepPasswordBadge").classList.add("is-active");
    }
}
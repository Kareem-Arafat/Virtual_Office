function openChangePasswordPage()
{
    window.location.href = "change-password.html";
}



function logout()
{
    sessionStorage.removeItem("token");
    sessionStorage.removeItem("user");

    window.location.href = "login.html";
}




async function deleteCurrentAccount()
{
    const token = sessionStorage.getItem("token");

    if(!token)
    {
        window.location.href = "login.html";
        return;
    }


    const sure = confirm("Are you sure you want to delete your account?");


    if(!sure)
    {
        return;
    }


    const response = await fetch
    (
        "http://localhost:8080/api/users/me",
        {
            method: "DELETE",

            headers:
            {
                "Authorization": "Bearer " + token
            }
        }
    );


    if(!response.ok)
    {

        return;
    }


    sessionStorage.removeItem("token");
    sessionStorage.removeItem("user");

    alert("Account deleted successfully");

    window.location.href = "login.html";
}
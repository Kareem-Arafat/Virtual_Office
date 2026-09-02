async function checkLoggedUser()
{
    const token = sessionStorage.getItem("token");

    if(!token)
    {
        window.location.href = "login.html";
        return;
    }

    const response = await fetch("http://localhost:8080/me",
    {
        headers:
        {
            "Authorization": "Bearer " + token
        }
    });

    if(response.status === 401 || response.status === 403)
    {
        sessionStorage.clear();
        window.location.href = "login.html";
    }
}

checkLoggedUser();
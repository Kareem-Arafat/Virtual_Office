document.addEventListener("DOMContentLoaded", function()
{
    applyTeamLeaderControls();
    loadEmployees();
});



function applyTeamLeaderControls()
{
    const currentUser = JSON.parse(sessionStorage.getItem("user") || "null");
    const panel = document.getElementById("teamManagement");
    if(panel)
    {
        panel.hidden = !currentUser || currentUser.role !== "TEAM_LEADER";
    }
}



async function loadEmployees()
{
    const token = sessionStorage.getItem("token");

    if (!token)
    {
        window.location.href = "login.html";
        return;
    }


    const empList = document.getElementById("empList");


    try
    {
        const response = await fetch
        (
            "/api/users/visible",
            {
                method: "GET",

                headers:
                {
                    "Authorization": "Bearer " + token
                }
            }
        );


        if (response.status === 401)
        {
            sessionStorage.clear();
            window.location.href = "login.html";
            return;
        }


        if (!response.ok)
        {
            empList.innerHTML = "<p>Failed to load employees.</p>";

            return;
        }


        const users = await response.json();

        displayEmployees(users);
    }
    catch (error)
    {

        empList.innerHTML = "<p>Cannot connect to server.</p>";
    }
}



function displayEmployees(users)
{
    const empList = document.getElementById("empList");
    const savedUser = sessionStorage.getItem("user");
    const currentUser = savedUser ? JSON.parse(savedUser) : null;


    empList.innerHTML = "";


    if (users.length === 0)
    {
        empList.innerHTML = "<p>No employees found.</p>";

        return;
    }


    for (const user of users)
    {
        const card = document.createElement("div");


        card.className = "emp-card";


        const canManage = currentUser && currentUser.role === "MANAGER" && Number(currentUser.id) !== Number(user.id);

        const canRemoveFromTeam = currentUser && currentUser.role === "TEAM_LEADER" && user.teamLeaderUsername === currentUser.username;


        let managementControls = "";


        if(canManage)
        {
            let developerSelected = "";
            let teamLeaderSelected = "";
            let managerSelected = "";

            if(user.role === "DEVELOPER")
            {
                developerSelected = "selected";
            }

            if(user.role === "TEAM_LEADER")
            {
                teamLeaderSelected = "selected";
            }

            if(user.role === "MANAGER")
            {
                managerSelected = "selected";
            }


            managementControls = `
                <select id="role-${user.id}" class="role-select">
                    <option value="DEVELOPER" ${developerSelected}>Developer</option>
                    <option value="TEAM_LEADER" ${teamLeaderSelected}>Team Leader</option>
                    <option value="MANAGER" ${managerSelected}>Manager</option>
                </select>

                <div class="emp-card-actions">
                    <button class="promote-btn update-role-btn">Update Role</button>
                    <button class="remove-btn remove-employee-btn">Remove Employee</button>
                </div>
            `;
        }
        else if(canRemoveFromTeam)
        {
            managementControls = `
                <div class="emp-card-actions">
                    <button class="remove-btn remove-team-member-btn">
                        Remove from My Team
                    </button>
                </div>
            `;
        }


        let teamLeaderInfo = "";

        if(user.teamLeaderUsername)
        {
            teamLeaderInfo = `
                <p>
                    <strong>Team Leader:</strong>
                    ${escapeEmployeeText(user.teamLeaderUsername)}
                </p>
            `;
        }


        card.innerHTML = `
            <div class="emp-card-header">
                <div>
                    <h3>${escapeEmployeeText(user.username)}</h3>
                    <p>${escapeEmployeeText(user.email)}</p>
                </div>
            </div>


            <div class="emp-card-body">

                <p>
                    <strong>Staff ID:</strong>
                    ${escapeEmployeeText(user.staffId || "Not assigned")}
                </p>

                <p>
                    <strong>Current Role:</strong>
                    ${escapeEmployeeText(user.role)}
                </p>

                ${teamLeaderInfo}

                ${managementControls}

            </div>
        `;


        if(canManage)
        {
            card.querySelector(".update-role-btn").onclick = function()
            {
                changeUserRole(user.id);
            };

            card.querySelector(".remove-employee-btn").onclick = function()
            {
                removeEmployee(user.id, user.username);
            };
        }


        if(canRemoveFromTeam)
        {
            card.querySelector(".remove-team-member-btn").onclick = function()
            {
                removeTeamMember(user.staffId);
            };
        }


        empList.appendChild(card);
    }
}



async function changeUserRole(userId)
{
    const currentUser = JSON.parse(sessionStorage.getItem("user") || "null");
    if(!currentUser || currentUser.role !== "MANAGER" || Number(currentUser.id) === Number(userId))
    {
        showEmployeeMessage("You cannot change this user's role.");
        return;
    }

    const token = sessionStorage.getItem("token");
    const roleElement = document.getElementById("role-" + userId);
    const role = roleElement.value;


    try
    {
        const response = await fetch
        (
            "/api/admin/users/"
            + userId
            + "/role?role="
            + encodeURIComponent(role),
            {
                method: "PUT",

                headers:
                {
                    "Authorization":"Bearer " + token
                }
            }
        );


        if (response.ok)
        {
            showEmployeeMessage("Role updated successfully.");

            loadEmployees();
        }
        else
        {
            const errorText = await response.text();


            showEmployeeMessage("Failed to update role.");
        }
    }
    catch (error)
    {

        showEmployeeMessage("Server error.");
    }
}



async function removeEmployee(userId,username)
{
    const currentUser = JSON.parse(sessionStorage.getItem("user") || "null");
    if(!currentUser || currentUser.role !== "MANAGER" || Number(currentUser.id) === Number(userId))
    {
        showEmployeeMessage("You cannot remove this user.");
        return;
    }
    const confirmed = confirm("Are you sure you want to remove " + username + "?");


    if (!confirmed)
    {
        return;
    }


    const token = sessionStorage.getItem("token");


    try
    {
        const response = await fetch
        (
            "/api/admin/users/"
            + userId,
            {
                method: "DELETE",

                headers:
                {
                    "Authorization":"Bearer " + token
                }
            }
        );


        if (response.ok)
        {
            showEmployeeMessage("Employee removed successfully.");


            loadEmployees();
        }
        else
        {
            const errorText = await response.text();


            showEmployeeMessage("Failed to remove employee.");
        }
    }
    catch (error)
    {
        showEmployeeMessage("Server error.");
    }
}



function showEmployeeMessage(message)
{
    const container = document.getElementById("toast-container");


    if (!container)
    {
        alert(message);
        return;
    }


    const toast = document.createElement("div");


    toast.className = "notification-toast";
    toast.textContent = message;


    container.appendChild(toast);


    setTimeout(
        function()
        {
            toast.remove();
        },
        3000
    );
}




function escapeEmployeeText(value)
{
    const element = document.createElement("div");
    if(value == null)
    {
        element.textContent = "";
    }
    else
    {
        element.textContent = String(value);
    }

    return element.innerHTML;
}




async function addTeamMember()
{
    const input = document.getElementById("teamMemberStaffId");
    let staffId = "";

    if(input)
    {
        staffId = input.value.trim();
    }
    
    if(!staffId)
    {
        showEmployeeMessage("Enter a Developer Staff ID.");
        return;
    }

    const response = await teamMemberRequest("POST", staffId);
    if(response && response.ok)
    {
        input.value = "";
        showEmployeeMessage("Developer added to your team.");
        loadEmployees();
    }
}




async function removeTeamMember(staffId)
{
    const response = await teamMemberRequest("DELETE", staffId);
    if(response && response.ok)
    {
        showEmployeeMessage("Developer removed from your team.");
        loadEmployees();
    }
}




async function teamMemberRequest(method, staffId)
{
    try
    {
        const response = await fetch("/api/team/members?staffId=" + encodeURIComponent(staffId),
        {
            method: method,
            headers: {"Authorization": "Bearer " + sessionStorage.getItem("token")}
        });

        if(!response.ok)
        {
            showEmployeeMessage((await response.text()) || "Team membership could not be updated.");
        }

        return response;
    }
    catch(error)
    {
        showEmployeeMessage("Cannot connect to the server.");
        return null;
    }
}


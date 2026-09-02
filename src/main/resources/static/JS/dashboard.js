document.addEventListener
(
    "DOMContentLoaded",
    function()
    {
        loadDashboardStatistics();
    }
);




async function loadDashboardStatistics()
{
    const token = sessionStorage.getItem("token");

    if(!token)
    {
        window.location.href = "login.html";
        return;
    }

    try
    {
        const headers = {"Authorization": "Bearer " + token};
        const responses = await Promise.all
        ([
            fetch("http://localhost:8080/me", {headers: headers}),
            fetch("http://localhost:8080/api/tasks", {headers: headers}),
            fetch("http://localhost:8080/api/users/visible", {headers: headers})
        ]);

        if(responses.some(response => response.status === 401))
        {
            sessionStorage.clear();
            window.location.href = "login.html";
            return;
        }

        if(responses.some(response => !response.ok))
        {
            showDashboardError("Failed to load dashboard data.");
            return;
        }

        const user = await responses[0].json();
        const tasks = await responses[1].json();
        const users = await responses[2].json();
        const stats = calculateTaskStatistics(tasks);

        sessionStorage.setItem("user", JSON.stringify(user));
        displayStatistics(stats);
        displayRoleDashboard(user, tasks, users, stats);
    }
    catch(error)
    {
        showDashboardError("Cannot connect to the server.");
    }
}





function calculateTaskStatistics(tasks)
{
    const completedTasks = tasks.filter(task => task.status === "DONE").length;
    const pendingTasks = tasks.filter(task => task.status === "TODO").length;
    const inProgressTasks = tasks.filter(task => task.status === "IN_PROGRESS").length;
    const today = new Date().toISOString().slice(0, 10);
    const overdueTasks = tasks.filter(task => task.status !== "DONE" && task.deadline && task.deadline < today).length;

    let completionPercentage = 0;

    if(tasks.length > 0)
    {
        completionPercentage = completedTasks * 100 / tasks.length;
    }

    return {
        totalTasks: tasks.length,
        completedTasks: completedTasks,
        pendingTasks: pendingTasks,
        inProgressTasks: inProgressTasks,
        overdueTasks: overdueTasks,
        completionPercentage: completionPercentage
    };
}





function displayRoleDashboard(user, tasks, users, stats)
{
    const newTaskButton = document.querySelector(".dashboard-hero .hero-action");

    if(newTaskButton)
    {
        if(user.role === "DEVELOPER")
        {
            newTaskButton.style.display = "none";
        }
        else
        {
            newTaskButton.style.display = "";
        }
    }


    let roleTitle = "My Work";

    if(user.role === "MANAGER")
    {
        roleTitle = "Company";
    }
    else if(user.role === "TEAM_LEADER")
    {
        roleTitle = "My Team";
    }

    setText("performanceTitle", roleTitle + " Completion Rate");


    const employeesTitle = document.getElementById("employeesStatTitle");
    const totalEmployees = document.getElementById("totalEmployees");

    let employeeCount = users.filter(item => item.role === "DEVELOPER").length;


    if(employeesTitle)
    {
        if(user.role === "DEVELOPER")
        {
            employeesTitle.textContent = "Overdue";
        }
        else
        {
            employeesTitle.textContent = "Employees";
        }
    }


    if(totalEmployees)
    {
        if(user.role === "DEVELOPER")
        {
            totalEmployees.textContent = stats.overdueTasks;
        }
        else
        {
            totalEmployees.textContent = employeeCount;
        }
    }


    if(user.role === "DEVELOPER")
    {
        setText("statsEmployeesCount", 1);
    }
    else
    {
        setText("statsEmployeesCount", employeeCount);
    }

    setText("statsCreatedByMe", stats.overdueTasks);

    setText("statsSummary", stats.completedTasks + " of " + stats.totalTasks + " visible tasks are complete. " + stats.overdueTasks + " overdue.");


    displayUpcomingTasks(tasks);
    displayPeopleStatistics(user, tasks, users);
    displayStatusBreakdown(stats);
}





function displayUpcomingTasks(tasks)
{
    const preview = document.getElementById("recentTaskPreview");
    if(!preview)
    {
        return;
    }

    const openTasks = tasks.filter(task => task.status !== "DONE");

    openTasks.sort((a, b) =>
    {
        const deadlineA = a.deadline || "9999";
        const deadlineB = b.deadline || "9999";

        return String(deadlineA).localeCompare(String(deadlineB));
    });


    if(openTasks.length > 0)
    {
        const firstTasks = openTasks.slice(0, 5);

        preview.innerHTML = firstTasks.map(task =>
        {
            const title = escapeDashboardText(task.title);
            const assignedTo = escapeDashboardText(task.assignedToUsername || "You");
            const deadline = escapeDashboardText(task.deadline || "No deadline");

            return `<p><strong>${title}</strong> · ${assignedTo} · ${deadline}</p>`;
        }).join("");
    }
    else
    {
        preview.innerHTML = "No open tasks.";
    }
}




function displayPeopleStatistics(user, tasks, users)
{
    const containers = [document.getElementById("managerPerformanceMeta"), document.getElementById("assigneeStatsList")].filter(Boolean);

    if(user.role === "DEVELOPER")
    {
        for(const container of containers)
        {
            container.innerHTML = "Your dashboard contains only your own tasks.";
        }
        return;
    }

    const employees = users.filter(item => item.role === "DEVELOPER");
    const teamLeaders = users.filter(item => item.role === "TEAM_LEADER");
    const teamLeadersCount = teamLeaders.length;


    let summary;

    if(user.role === "MANAGER")
    {
        summary = `<p><strong>Company:</strong> ${employees.length} developers, ${teamLeadersCount} team leaders</p>`;
    }
    else
    {
        summary = `<p><strong>My team:</strong> ${employees.length} developers</p>`;
    }


    let rows = summary;


    for(const employee of employees)
    {
        const employeeTasks = tasks.filter(task =>task.assignedToStaffId === employee.staffId);

        const stats = calculateTaskStatistics(employeeTasks);

        const username = escapeDashboardText(employee.username);
        const openTasks = stats.pendingTasks + stats.inProgressTasks;

        rows += `
            <p>
                <strong>${username}</strong>:
                ${stats.totalTasks} total,
                ${stats.completedTasks} completed,
                ${openTasks} open,
                ${stats.overdueTasks} overdue
            </p>
        `;
    }


    for(const container of containers)
    {
        if(rows)
        {
            container.innerHTML = rows;
        }
        else
        {
            container.innerHTML = "No employee task data available.";
        }

        container.classList.add("show");
    }
}




function escapeDashboardText(value)
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





function displayStatusBreakdown(stats)
{
    const total = stats.totalTasks || 1;
    const values =
    {
        completedStatusPercent: stats.completedTasks * 100 / total,
        progressStatusPercent: stats.inProgressTasks * 100 / total,
        pendingStatusPercent: stats.pendingTasks * 100 / total
    };

    for(const id in values)
    {
        setText(id, values[id].toFixed(1) + "%");
    }

    const bars = {completedStatusBar: values.completedStatusPercent, progressStatusBar: values.progressStatusPercent, pendingStatusBar: values.pendingStatusPercent};
    for(const id in bars)
    {
        const element = document.getElementById(id);
        if(element)
        {
            element.style.width = bars[id] + "%";
        }
    }
}





function showDashboardError(message)
{
    const preview = document.getElementById("recentTaskPreview") || document.getElementById("statsSummary");
    if(preview)
    {
        preview.textContent = message;
    }
}




function goToTasksByStatus(status)
{
    sessionStorage.setItem("taskFilter", status);
    window.location.href = "tasks.html";
}




function exportStatisticsPdf()
{
    window.print();
}





function displayStatistics(stats)
{
    setText("totalTasks",stats.totalTasks);
    setText("completedTasks",stats.completedTasks);
    setText("pendingTasks",stats.pendingTasks);
    setText("inProgressTasks",stats.inProgressTasks);


    const percentage = Number(stats.completionPercentage|| 0);


    setText("completionRate",percentage.toFixed(1)+ "%");
    setText("statsTotalTasks",stats.totalTasks);
    setText("statsCompletedTasks",stats.completedTasks);
    setText("statsPendingTasks", stats.pendingTasks);
    setText("statsProgressTasks", stats.inProgressTasks);
    setText("statsCompletionRate", percentage.toFixed(1) + "%");


    updateCompletionBar(percentage);
}



function setText(elementId, value)
{
    const element = document.getElementById(elementId);


    if (element)
    {
        element.textContent = value;
    }
}




function updateCompletionBar(percentage)
{
    const completionBar = document.getElementById("completionBar");


    if (completionBar)
    {
        completionBar.style.width = percentage + "%";
    }


    const statsCompletionBar = document.getElementById("statsCompletionBar");


    if (statsCompletionBar)
    {
        statsCompletionBar.style.width = percentage + "%";
    }
}

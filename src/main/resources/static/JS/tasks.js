const TASK_API = "http://localhost:8080/api/tasks";
let loadedTasks = [];



function getCurrentUser()
{
    const user = sessionStorage.getItem("user");

    if(!user)
    {
        return null;
    }

    return JSON.parse(user);
}




function getToken()
{
    return sessionStorage.getItem("token");
}







function applyTaskPermissions()
{
    const user = getCurrentUser();
    const taskInput = document.querySelector(".task-input");

    if(!user)
    {
        return;
    }

    if(user.role === "MANAGER" || user.role === "TEAM_LEADER")
    {
        if(taskInput)
        {
            taskInput.style.display = "";
        }
    }
    else
    {
        if(taskInput)
        {
            taskInput.style.display = "none";
        }
    }
}







async function createTask()
{
    const token = getToken();
    const currentUser = getCurrentUser();

    if(!token)
    {
        window.location.href = "login.html";
        return;
    }

    if(!currentUser || (currentUser.role !== "MANAGER" && currentUser.role !== "TEAM_LEADER"))
    {
        alert("You do not have permission to create tasks");
        return;
    }


    const title = document.getElementById("taskTitle").value.trim();
    const description = document.getElementById("taskDesc").value.trim();
    const assignedToStaffId = document.getElementById("taskAssigneeStaffId").value.trim();
    const deadline = document.getElementById("taskDeadline").value;


    if(!title ||!description ||!assignedToStaffId ||!deadline)
    {
        alert("Please fill all task fields");
        return;
    }


    const response = await fetch
    (
        TASK_API,
        {
            method: "POST",

            headers:
            {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + token
            },

            body: JSON.stringify(
            {
                title: title,
                description: description,
                assignedToStaffId: assignedToStaffId,
                deadline: deadline
            })
        }
    );


    if(!response.ok)
    {
        const error = await response.text();

        alert(error || "Could not create task");

        return;
    }


    document.getElementById("taskTitle").value = "";
    document.getElementById("taskDesc").value = "";
    document.getElementById("taskAssigneeStaffId").value = "";
    document.getElementById("taskDeadline").value = "";

    await loadTasks();
}






async function loadTasks()
{
    const token = getToken();

    if(!token)
    {
        window.location.href = "login.html";
        return;
    }


    const response = await fetch
    (
        TASK_API,
        {
            method: "GET",

            headers:
            {
                "Authorization": "Bearer " + token
            }
        }
    );


    if(!response.ok)
    {
        if(response.status === 401)
        {
            sessionStorage.removeItem("token");
            sessionStorage.removeItem("user");

            window.location.href = "login.html";
            return;
        }



        return;
    }

    const tasks = await response.json();

    loadedTasks = tasks;
    displayTasks(tasks);
    updateTaskFilterCounts(tasks);

    const savedFilter = sessionStorage.getItem("taskFilter");

    if(savedFilter)
    {
        sessionStorage.removeItem("taskFilter");
        const statusMap = {completed: "DONE", pending: "TODO", in_progress: "IN_PROGRESS", all: "all"};
        setTaskFilter(statusMap[savedFilter] || "all");
    }
}







function displayTasks(tasks)
{
    const taskList = document.getElementById("taskList");
    const user = getCurrentUser();


    taskList.innerHTML = "";

    if(!tasks.length)
    {
        taskList.innerHTML = "<p>No tasks found.</p>";
        return;
    }


    for(const task of tasks)
    {
        let actions = "";

        if(user.role === "MANAGER" || user.role === "TEAM_LEADER")
        {
            actions = `
                <button
                    type="button"
                    onclick="editTask(${task.id})"
                >
                    Edit
                </button>

                <button
                    type="button"
                    onclick="deleteTask(${task.id})"
                >
                    Delete
                </button>
            `;
        }
        else if(user.role === "DEVELOPER")
        {
            if(task.status === "DONE")
            {
                actions = `
                    <strong>✓ Completed</strong>

                    <button
                        type="button"
                        onclick="changeTaskStatus(${task.id}, 'TODO')"
                    >
                        Mark Not Done
                    </button>
                `;
            }
            else
            {
                actions = `
                    <button
                        type="button"
                        onclick="changeTaskStatus(${task.id}, 'IN_PROGRESS')"
                    >
                        In Progress
                    </button>

                    <button
                        type="button"
                        onclick="changeTaskStatus(${task.id}, 'DONE')"
                    >
                        Done
                    </button>
                `;
            }
        }

        const taskHTML = `
            <li class="task-card">

                <h3>
                    ${escapeTaskText(task.title)}
                </h3>

                <p>
                    ${escapeTaskText(task.description)}
                </p>

                <p>
                    Deadline:
                    ${escapeTaskText(task.deadline || "No deadline")}
                </p>

                <p>
                    Status:
                    ${escapeTaskText(task.status)}
                </p>

                ${task.assignedToUsername ? `<p>Assigned to: ${escapeTaskText(task.assignedToUsername)} (${escapeTaskText(task.assignedToStaffId)})</p>` : ""}

                <small>
                    Created:
                    ${escapeTaskText(formatCreatedAt(task.createdAt))}
                </small>

                <div class="task-actions">
                    ${actions}
                </div>

            </li>
        `;


        taskList.innerHTML += taskHTML;
    }
}







async function changeTaskStatus(taskId, status)
{
    const token = getToken();

    const response = await fetch
    (
        `${TASK_API}/${taskId}/status`,
        {
            method: "PATCH",

            headers:
            {
                "Content-Type": "application/json",

                "Authorization":"Bearer " + token
            },

            body: JSON.stringify(
            {
                status: status
            })
        }
    );


    if(!response.ok)
    {
        const error = await response.text();

        alert(error || "Could not update task status");

        return;
    }

    await loadTasks();
}






async function editTask(taskId)
{
    const token = getToken();

    const response = await fetch
    (
        TASK_API,
        {
            headers:
            {
                "Authorization":"Bearer " + token
            }
        }
    );


    if(!response.ok)
    {
        alert("Could not load task");
        return;
    }

    const tasks = await response.json();
    const task =tasks.find(t => Number(t.id) === Number(taskId));

    if(!task)
    {
        alert("Task not found");

        return;
    }

    const title = prompt("Task title:",task.title);


    if(title === null)
    {
        return;
    }

    const description = prompt("Task description:", task.description);

    if(description === null)
    {
        return;
    }

    const deadline = prompt("Deadline (YYYY-MM-DD):", task.deadline || "");

    if(deadline === null)
    {
        return;
    }

    const status = prompt("Status: TODO, IN_PROGRESS, DONE", task.status);

    if(status === null)
    {
        return;
    }

    const validStatuses = ["TODO", "IN_PROGRESS", "DONE"];
    const normalizedStatus = status.trim().toUpperCase();


    if(!validStatuses.includes(normalizedStatus))
    {
        alert("Status must be TODO, IN_PROGRESS or DONE");
        return;
    }


    const updateResponse = await fetch
    (
        `${TASK_API}/${taskId}`,
        {
            method: "PUT",

            headers:
            {
                "Content-Type": "application/json",

                "Authorization": "Bearer " + token
            },

            body: JSON.stringify(
            {
                title: title.trim(),
                description: description.trim(),
                status: normalizedStatus,
                deadline: deadline
            })
        }
    );


    if(!updateResponse.ok)
    {
        const error = await updateResponse.text();

        alert(error || "Could not edit task");

        return;
    }

    await loadTasks();
}







async function deleteTask(taskId)
{
    const token = getToken();
    const confirmDelete =confirm("Are you sure you want to delete this task?");


    if(!confirmDelete)
    {
        return;
    }


    const response = await fetch
    (
        `${TASK_API}/${taskId}`,
        {
            method: "DELETE",

            headers:
            {
                "Authorization":
                    "Bearer " + token
            }
        }
    );


    if(!response.ok)
    {
        const error = await response.text();

        alert(error || "Could not delete task");

        return;
    }

    await loadTasks();
}






function formatCreatedAt(value)
{
    if(!value)
    {
        return "";
    }

    const date = new Date(value);


    if(Number.isNaN(date.getTime()))
    {
        return value;
    }

    return date.toLocaleString();
}






function setTaskFilter(status)
{
    const filteredTasks = status === "all" ? loadedTasks : loadedTasks.filter(task => task.status === status);
    displayTasks(filteredTasks);

    const scope = document.getElementById("activeTaskScope");
    if(scope)
    {
        scope.textContent = status === "all" ? "Showing all visible tasks" : "Showing " + status + " tasks";
    }
}




function escapeTaskText(value)
{
    const element = document.createElement("div");
    element.textContent = value == null ? "" : String(value);
    return element.innerHTML;
}





function updateTaskFilterCounts(tasks)
{
    const counts =
    {
        filterAllCount: tasks.length,
        filterPendingCount: tasks.filter(task => task.status === "TODO").length,
        filterProgressCount: tasks.filter(task => task.status === "IN_PROGRESS").length,
        filterCompletedCount: tasks.filter(task => task.status === "DONE").length
    };

    for(const id in counts)
    {
        const element = document.getElementById(id);
        if(element)
        {
            element.textContent = counts[id];
        }
    }
}





document.addEventListener("DOMContentLoaded", function()
{
    applyTaskPermissions();
    loadTasks();
});

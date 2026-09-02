let notificationStompClient = null;
let notificationRealtimeStarted = false;
let notifications = [];


document.addEventListener
(
    "DOMContentLoaded",
    function()
    {
        addNotificationCloseButton();
        loadNotifications();
        startNotificationRealtime();
    }
);



function addNotificationCloseButton()
{
    document.querySelectorAll(".notif-dropdown-head").forEach(function(header)
    {
        if(header.querySelector(".notif-close"))
        {
            return;
        }

        const closeButton = document.createElement("button");
        closeButton.type = "button";
        closeButton.className = "notif-close";
        closeButton.setAttribute("aria-label", "Close notifications");
        closeButton.textContent = "x";
        closeButton.onclick = closeNotifDropdown;
        header.appendChild(closeButton);
    });
}



function closeNotifDropdown(event)
{
    if(event)
    {
        event.stopPropagation();
    }

    const dropdown = document.getElementById("notifDropdown");

    if(dropdown)
    {
        dropdown.classList.remove("show-notif");
    }
}




function startNotificationRealtime()
{
    if (notificationRealtimeStarted)
    {
        return;
    }

    if(typeof SockJS === "undefined" || typeof Stomp === "undefined")
    {
        return;
    }


    const token = sessionStorage.getItem("token");


    if (!token)
    {
        return;
    }


    const userData = sessionStorage.getItem("user");


    if (!userData)
    {
        return;
    }


    const user = JSON.parse(userData);


    if (!user.username)
    {
        return;
    }


    const socket =new SockJS("http://localhost:8080/ws");


    notificationStompClient = Stomp.over(socket);


    notificationStompClient.connect(
    {
        "Authorization":"Bearer " + token
    },

        function()
        {
            notificationRealtimeStarted = true;


            notificationStompClient.subscribe("/topic/notifications/" + user.username,

                function(message)
                {
                    const notification = JSON.parse(message.body);

                    notifications.unshift(notification);


                    updateNotificationUI();

                    showNotificationToast(notification.message);
                }
            );
        },

        function(error)
        {
            notificationRealtimeStarted = false;
        }
    );
}



async function loadNotifications()
{
    const token = sessionStorage.getItem("token");


    if (!token)
    {
        return;
    }


    try
    {
        const response = await fetch
        (
            "http://localhost:8080/api/notifications",
            {
                method: "GET",
                headers:
                {
                    "Authorization":"Bearer " + token
                }
            }
        );


        if (!response.ok)
        {
            return;
        }


        notifications = await response.json();


        updateNotificationUI();
    }
    catch (error){}
}



function updateNotificationUI()
{
    const badge = document.getElementById("notifBadge");
    const list = document.getElementById("notifList");
    const unreadCount = notifications.filter(notification =>!notification.read && !notification.isRead).length;


    if (badge)
    {
        badge.textContent = unreadCount;

        badge.style.display = unreadCount > 0 ? "flex" : "none";
    }


    if (!list)
    {
        return;
    }


    list.innerHTML = "";


    if (notifications.length === 0)
    {
        list.innerHTML = "<li>No notifications</li>";

        return;
    }


    notifications.forEach
    (
        function(notification)
        {
            const item = document.createElement("li");


            item.textContent = notification.message;


            if (!notification.read && !notification.isRead)
            {
                item.classList.add("unread");
            }


            item.addEventListener("click", function()
            {
                markNotificationAsRead(notification.id);
            });

            list.appendChild(item);
        }
    );
}




async function markNotificationAsRead(notificationId)
{
    const token = sessionStorage.getItem("token");


    if (!token)
    {
        return;
    }


    try
    {
        const response = await fetch(
            "http://localhost:8080/api/notifications/"
            + notificationId
            + "/read",
            {
                method: "PUT",

                headers:
                {
                    "Authorization":"Bearer " + token
                }
            }
         );


        if (!response.ok)
        {
            return;
        }


        const notification = notifications.find(item => item.id === notificationId);


        if (notification)
        {
            notification.read = true;
            notification.isRead = true;
        }


        updateNotificationUI();
    }
    catch (error){}
}




function showNotificationToast(message)
{
    let container = document.getElementById("toast-container");


    if (!container)
    {
        container = document.createElement("div");

        container.id = "toast-container";


        document.body.appendChild(container);
    }


    const toast = document.createElement("div");


    toast.className = "notification-toast";


    toast.textContent = message;


    container.appendChild(toast);


    setTimeout(
        function()
        {
            toast.remove();
        }, 4000
    );
}




function toggleNotifDropdown()
{
    const dropdown = document.getElementById("notifDropdown");

    if(!dropdown)
    {
        return;
    }

    const isOpening = !dropdown.classList.contains("show-notif");
    dropdown.classList.toggle("show-notif");

    if(isOpening)
    {
        markAllNotificationsRead();
    }
}




async function markAllNotificationsRead(event)
{
    if(event)
    {
        event.stopPropagation();
    }

    const token = sessionStorage.getItem("token");

    if(!token)
    {
        return;
    }

    try
    {
        const response = await fetch("http://localhost:8080/api/notifications/read-all",
        {
            method: "PUT",
            headers:
            {
                "Authorization": "Bearer " + token
            }
        });

        if(!response.ok)
        {
            showNotificationToast("Could not mark notifications as read.");
            return;
        }

        for(const notification of notifications)
        {
            notification.read = true;
            notification.isRead = true;
        }

        updateNotificationUI();
    }
    catch(error)
    {
        showNotificationToast("Could not update notifications.");
    }
}

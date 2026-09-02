let stompClient = null;
let currentRoomId = null;
let currentSubscription = null;
let isWebSocketConnected = false;
let webSocketConnectionPromise = null;
let currentRoom = null;
let mediaRecorder = null;
let audioChunks = [];
let microphoneStream = null;
let recordingRoomId = null;

const currentRoomMessages = new Map();


function connectWebSocket()
{
    const token = sessionStorage.getItem("token");

    if(!token)
    {
        window.location.href = "login.html";

        return Promise.reject(new Error("Authentication is required"));
    }


    if(stompClient && stompClient.connected)
    {
        isWebSocketConnected = true;

        return Promise.resolve();
    }


    if(webSocketConnectionPromise)
    {
        return webSocketConnectionPromise;
    }


    webSocketConnectionPromise = new Promise(function(resolve, reject)
    {
        if(typeof SockJS === "undefined" || typeof Stomp === "undefined")
        {
            webSocketConnectionPromise = null;

            reject(new Error("Chat libraries did not load"));

            return;
        }


        const socket = new SockJS("http://localhost:8080/ws");

        stompClient = Stomp.over(socket);


        const headers = 
        {
            "Authorization": "Bearer " + token
        };


        stompClient.connect
        (
            headers,

            function()
            {
                isWebSocketConnected = true;

                if(currentRoomId != null)
                {
                    subscribeToRoom(currentRoomId);
                }

                webSocketConnectionPromise = null;

                resolve();
            },

            function()
            {
                isWebSocketConnected = false;
                currentSubscription = null;
                webSocketConnectionPromise = null;

                reject(
                    new Error("Chat connection failed")
                );
            }
        );
    });


    return webSocketConnectionPromise;
}





async function loadRooms()
{
    const token = sessionStorage.getItem("token");

    if(!token)
    {
        window.location.href = "login.html";
        return;
    }


    const currentUser = JSON.parse(sessionStorage.getItem("user") || "null");


    let roomsUrl = "/api/rooms";

    if(currentUser && currentUser.role === "MANAGER")
    {
        roomsUrl = "/api/rooms/all";
    }


    const response = await fetch
    (
        "http://localhost:8080" + roomsUrl,
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
        showRoomMessage("Failed to load rooms.");
        return;
    }


    const rooms = await response.json();

    displayRooms(rooms);
}




function displayRooms(rooms)
{
    const roomList = document.getElementById("roomList");

    roomList.innerHTML = "";


    if(rooms.length === 0)
    {
        roomList.innerHTML =
            '<p class="room-list-empty">No rooms available. Create or join a room first.</p>';

        resetCurrentRoom();

        return;
    }


    const currentUser = JSON.parse(
        sessionStorage.getItem("user") || "null"
    );


    for(const room of rooms)
    {
        const roomDiv = document.createElement("div");

        roomDiv.className = "room room-item";


        const roomName = document.createElement("strong");
        roomName.className = "room-name";
        roomName.textContent = room.name;


        const roomDescription = document.createElement("p");

        if(room.description)
        {
            roomDescription.textContent = room.description;
        }
        else
        {
            roomDescription.textContent = "";
        }


        const roomDetails = document.createElement("small");

        roomDetails.textContent =
            room.membersCount +
            " members · Created by " +
            room.createdByUsername;


        roomDiv.append(
            roomName,
            roomDescription,
            roomDetails
        );


        if(room.currentUserMember)
        {
            roomDiv.onclick = function()
            {
                openRoom(room);
            };
        }
        else
        {
            roomDiv.title =
                "Room metadata only. You are not a member.";
        }


        if(currentUser && currentUser.role === "MANAGER")
        {
            const deleteButton = document.createElement("button");

            deleteButton.className = "room-delete-button";
            deleteButton.type = "button";
            deleteButton.title = "Delete room";

            deleteButton.setAttribute(
                "aria-label",
                "Delete " + room.name
            );

            deleteButton.innerHTML =
                '<i class="fa-solid fa-trash"></i><span>Delete</span>';


            deleteButton.onclick = function(event)
            {
                event.stopPropagation();

                deleteRoomById(room.id);
            };


            roomDiv.appendChild(deleteButton);
        }


        roomList.appendChild(roomDiv);
    }
}



function openRoom(room)
{
    if(!room || !room.currentUserMember)
    {
        resetCurrentRoom();

        showRoomMessage("You must be a room member before you can chat.");

        return;
    }


    currentRoom = room;
    currentRoomId = room.id;

    currentRoomMessages.clear();


    document.getElementById("roomTitle").textContent = room.name;
    document.getElementById("activeRoomName").textContent = room.name;
    document.getElementById("chatBox").innerHTML = '<p class="room-empty-state">Loading messages...</p>';
    document.getElementById("msgCount").textContent = "0";


    loadRoomMessages(room.id);

    loadRoomMembers();

    applyRoomActions();


    if(isWebSocketConnected)
    {
        subscribeToRoom(room.id);
    }
}




function subscribeToRoom(roomId)
{
    if (stompClient == null || !isWebSocketConnected)
    {
        return;
    }

    if (currentSubscription != null)
    {
        currentSubscription.unsubscribe();
    }

    currentSubscription = stompClient.subscribe
    (
        "/topic/room/" + roomId,
        function(message)
        {
            const chatMessage = JSON.parse(message.body);

            if(Number(chatMessage.roomId) === Number(currentRoomId))
            {
                displayMessage(chatMessage);
            }
        }
    );
}




async function loadRoomMessages(roomId)
{
    const token = sessionStorage.getItem("token");

    if(!token)
    {
        window.location.href = "login.html";
        return;
    }


    const response = await fetch
    (
        "http://localhost:8080/api/rooms/" + roomId + "/messages",
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
        if(response.status === 403)
        {
            showRoomMessage("You are not allowed to read this room.");
        }
        else
        {
            showRoomMessage("Failed to load messages.");
        }

        return;
    }


    const messages = await response.json();


    if(Number(currentRoomId) !== Number(roomId))
    {
        return;
    }


    for(const message of messages)
    {
        const key = getMessageKey(message);

        currentRoomMessages.set(key, message);
    }


    renderRoomMessages();
}




async function sendMessage(event)
{
    if(event)
    {
        event.preventDefault();
    }


    if(currentRoomId == null)
    {
        showRoomMessage("Select a room first.");
        return;
    }


    const input = document.getElementById("msgInput");
    const sendButton = document.getElementById("sendMessageButton");

    const content = input.value.trim();


    if(content === "")
    {
        return;
    }


    const roomId = currentRoomId;

    input.disabled = true;

    if(sendButton)
    {
        sendButton.disabled = true;
    }


    try
    {
        connectWebSocket();


        const response = await fetch
        (
            "http://localhost:8080/api/rooms/" + roomId + "/messages",
            {
                method: "POST",
                headers:
                {
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + sessionStorage.getItem("token")
                },
                body: JSON.stringify(
                {
                    content: content
                })
            }
        );


        if(!response.ok)
        {
            const errorText = await response.text();

            let message = "Message request failed";

            if(errorText)
            {
                message = errorText;
            }

            throw new Error(message);
        }


        const savedMessage = await response.json();


        if(Number(currentRoomId) === Number(roomId))
        {
            displayMessage(savedMessage);
        }


        input.value = "";
        input.focus();
    }
    catch(error)
    {
        showRoomMessage("Message was not sent. Check the server connection and try again.");
    }
    finally
    {
        input.disabled = false;

        if(sendButton)
        {
            sendButton.disabled = false;
        }
    }
}




function displayMessage(chatMessage)
{
    if(!chatMessage)
    {
        return;
    }

    if(currentRoomId == null)
    {
        return;
    }

    if(Number(chatMessage.roomId) !== Number(currentRoomId))
    {
        return;
    }


    const messageKey = getMessageKey(chatMessage);

    if(currentRoomMessages.has(messageKey))
    {
        return;
    }


    currentRoomMessages.set(messageKey, chatMessage);

    appendMessageElement(chatMessage);

    updateMessageCount();
}




function getMessageKey(chatMessage)
{
    if(chatMessage.id != null)
    {
        return "id-" + chatMessage.id;
    }


    const parts = 
    [
        chatMessage.roomId,
        chatMessage.senderUsername,
        chatMessage.timestamp,
        chatMessage.content
    ];

    return "fallback-" + parts.join("|");
}





function renderRoomMessages()
{
    const chatBox = document.getElementById("chatBox");

    chatBox.innerHTML = "";


    const messages = Array.from(currentRoomMessages.values());


    messages.sort(function(first, second)
    {
        let firstTime = 0;
        let secondTime = 0;

        if(first.timestamp)
        {
            firstTime = new Date(first.timestamp).getTime();
        }

        if(second.timestamp)
        {
            secondTime = new Date(second.timestamp).getTime();
        }

        return firstTime - secondTime;
    });


    for(const message of messages)
    {
        appendMessageElement(message);
    }


    updateMessageCount();

    chatBox.scrollTop = chatBox.scrollHeight;
}




function appendMessageElement(chatMessage)
{
    const chatBox = document.getElementById("chatBox");

    const messageDiv = document.createElement("div");
    messageDiv.className = "message";


    if(chatMessage.id != null)
    {
        messageDiv.dataset.messageId = chatMessage.id;
    }


    const sender = document.createElement("strong");
    sender.className = "msg-sender";
    sender.textContent = chatMessage.senderUsername;

    messageDiv.appendChild(sender);


    if(chatMessage.audioData && chatMessage.mediaType)
    {
        const mediaSource =
            "data:" +
            chatMessage.mediaType +
            ";base64," +
            chatMessage.audioData;


        if(chatMessage.mediaType.startsWith("image/"))
        {
            const image = document.createElement("img");

            image.src = mediaSource;
            image.alt = "Room image sent by " + chatMessage.senderUsername;
            image.loading = "lazy";

            messageDiv.appendChild(image);
        }

        else if(chatMessage.mediaType.startsWith("video/"))
        {
            const video = document.createElement("video");

            video.controls = true;
            video.src = mediaSource;

            messageDiv.appendChild(video);
        }

        else
        {
            const audio = document.createElement("audio");

            audio.controls = true;
            audio.src = mediaSource;

            messageDiv.appendChild(audio);
        }
    }

    else
    {
        const text = document.createElement("p");

        text.className = "msg-text";

        if(chatMessage.content)
        {
            text.textContent = chatMessage.content;
        }
        else
        {
            text.textContent = "";
        }

        messageDiv.appendChild(text);
    }


    const time = document.createElement("small");

    time.className = "msg-time";
    time.textContent = formatMessageTime(chatMessage.timestamp);

    messageDiv.appendChild(time);


    chatBox.appendChild(messageDiv);

    chatBox.scrollTop = chatBox.scrollHeight;
}




function updateMessageCount()
{
    const count = document.getElementById("msgCount");

    if(count)
    {
        count.textContent = currentRoomMessages.size;
    }
}




async function addRoom()
{
    let currentUser = null;

    const savedUser = sessionStorage.getItem("user");

    if(savedUser)
    {
        currentUser = JSON.parse(savedUser);
    }


    if(!currentUser)
    {
        showRoomMessage("You do not have permission to create rooms.");
        return;
    }


    if(currentUser.role !== "MANAGER" && currentUser.role !== "TEAM_LEADER")
    {
        showRoomMessage("You do not have permission to create rooms.");
        return;
    }


    const name = prompt("Room name:");

    if(name === null)
    {
        return;
    }

    if(name.trim() === "")
    {
        return;
    }


    let description = prompt("Description (optional):");

    if(description === null)
    {
        description = "";
    }


    const roomData = 
    {
        name: name.trim(),
        description: description.trim()
    };


    const response = await roomRequest("/api/rooms", "POST", roomData);


    if(response && response.ok)
    {
        showRoomMessage("Room created successfully.");

        loadRooms();
    }
}




function formatMessageTime(timestamp)
{
    if(!timestamp)
    {
        return "";
    }


    const value = new Date(timestamp);

    const time = value.getTime();


    if(Number.isNaN(time))
    {
        return timestamp;
    }
    else
    {
        return value.toLocaleString();
    }
}




async function toggleVoiceRecording()
{
    const button = document.getElementById("voiceButton");


    if(mediaRecorder && mediaRecorder.state === "recording")
    {
        mediaRecorder.stop();

        button.classList.remove("recording");

        button.innerHTML =
            '<i class="fa-solid fa-microphone"></i> Voice';

        return;
    }


    if(currentRoomId == null)
    {
        showRoomMessage("Select a room first.");
        return;
    }


    if(!navigator.mediaDevices)
    {
        showRoomMessage(
            "Voice recording is not supported by this browser."
        );

        return;
    }


    if(!window.MediaRecorder)
    {
        showRoomMessage(
            "Voice recording is not supported by this browser."
        );

        return;
    }


    try
    {
        const options = {
            audio: true
        };

        microphoneStream =
            await navigator.mediaDevices.getUserMedia(options);


        audioChunks = [];


        mediaRecorder =
            new MediaRecorder(microphoneStream);


        mediaRecorder.ondataavailable = function(event)
        {
            if(event.data.size > 0)
            {
                audioChunks.push(event.data);
            }
        };


        mediaRecorder.onstop = uploadVoiceMessage;

        recordingRoomId = currentRoomId;


        mediaRecorder.start();


        button.classList.add("recording");

        button.innerHTML =
            '<i class="fa-solid fa-stop"></i> Stop';
    }
    catch(error)
    {
        showRoomMessage(
            "Microphone permission is required to record a voice message."
        );
    }
}



async function uploadVoiceMessage()
{
    let mediaType = "audio/webm";

    if(mediaRecorder.mimeType)
    {
        mediaType = mediaRecorder.mimeType;
    }


    const recording = new Blob
    (
        audioChunks,
        {
            type: mediaType
        }
    );


    const roomId = recordingRoomId;

    recordingRoomId = null;


    if(microphoneStream)
    {
        const tracks = microphoneStream.getTracks();

        for(const track of tracks)
        {
            track.stop();
        }
    }


    microphoneStream = null;


    const data = new FormData();

    data.append("media", recording, "voice-message.webm");


    try
    {
        const response = await fetch
        (
            "http://localhost:8080/api/rooms/" + roomId + "/media",
            {
                method: "POST",

                headers:
                {
                    "Authorization": "Bearer " + sessionStorage.getItem("token")
                },

                body: data
            }
        );


        if(!response.ok)
        {
            const errorText = await response.text();

            if(errorText)
            {
                showRoomMessage(errorText);
            }
            else
            {
                showRoomMessage("Voice message could not be sent.");
            }

            return;
        }


        const savedMessage = await response.json();


        if(Number(currentRoomId) === Number(roomId))
        {
            displayMessage(savedMessage);
        }
    }
    catch(error)
    {
        showRoomMessage("Cannot connect to the server.");
    }
}



function chooseRoomMedia()
{
    if(currentRoomId == null)
    {
        showRoomMessage("Select a room first.");
        return;
    }

    document.getElementById("roomMediaInput").click();
}




async function uploadRoomMedia(event)
{
    const file = event.target.files[0];
    const roomId = currentRoomId;
    const mediaButton = document.getElementById("mediaButton");


    if(!file || roomId == null)
    {
        event.target.value = "";
        return;
    }


    const isImage = file.type.startsWith("image/");
    const isVideo = file.type.startsWith("video/");


    if(!isImage && !isVideo)
    {
        showRoomMessage("Choose an image or video file.");

        event.target.value = "";

        return;
    }


    let maximumSize = 5 * 1024 * 1024;

    if(isVideo)
    {
        maximumSize = 15 * 1024 * 1024;
    }


    if(file.size > maximumSize)
    {
        if(isVideo)
        {
            showRoomMessage("Video must be 15 MB or smaller.");
        }
        else
        {
            showRoomMessage("Image must be 5 MB or smaller.");
        }

        event.target.value = "";

        return;
    }


    const data = new FormData();

    data.append("media", file);


    if(mediaButton)
    {
        mediaButton.disabled = true;
    }


    try
    {
        const response = await fetch
        (
            "http://localhost:8080/api/rooms/" + roomId + "/media",
            {
                method: "POST",

                headers:
                {
                    "Authorization": "Bearer " + sessionStorage.getItem("token")
                },

                body: data
            }
        );


        if(!response.ok)
        {
            const errorText = await response.text();

            let message = "Media could not be sent";

            if(errorText)
            {
                message = errorText;
            }

            throw new Error(message);
        }


        const savedMessage = await response.json();


        if(Number(currentRoomId) === Number(roomId))
        {
            displayMessage(savedMessage);
        }
    }
    catch(error)
    {
        showRoomMessage("Photo or video was not sent. Check the server connection and file size.");
    }
    finally
    {
        event.target.value = "";

        if(mediaButton)
        {
            mediaButton.disabled = false;
        }
    }
}



async function promptAddMember()
{
    if(!currentRoom)
    {
        showRoomMessage("Only the room creator can add members.");
        return;
    }


    if(!currentRoom.currentUserCreator)
    {
        showRoomMessage("Only the room creator can add members.");
        return;
    }


    const input = document.getElementById("roomMemberStaffId");

    let staffId = "";

    if(input)
    {
        staffId = input.value.trim();
    }


    if(staffId === "")
    {
        showRoomMessage("Enter a Staff ID first.");
        return;
    }


    const path = "/api/rooms/" + currentRoomId + "/members?staffId=" + encodeURIComponent(staffId);


    const response = await roomRequest(path, "POST");


    if(response && response.ok)
    {
        input.value = "";

        showRoomMessage("Member added successfully.");

        loadRoomMembers();
        loadRooms();
    }
}



async function loadRoomMembers()
{
    const memberList = document.getElementById("memberList");


    if(!currentRoomId)
    {
        return;
    }


    if(!memberList)
    {
        return;
    }


    const response = await roomRequest("/api/rooms/" + currentRoomId + "/members", "GET");


    if(!response)
    {
        return;
    }


    if(!response.ok)
    {
        return;
    }


    const members = await response.json();


    let currentUser = null;

    const savedUser = sessionStorage.getItem("user");

    if(savedUser)
    {
        currentUser = JSON.parse(savedUser);
    }


    memberList.innerHTML = "";


    for(const member of members)
    {
        const item = document.createElement("li");

        item.textContent = member.username + " (" + member.staffId + ")";


        let canRemove = false;


        if(currentRoom.currentUserCreator && currentUser)
        {
            if(Number(member.id) !== Number(currentUser.id))
            {
                canRemove = true;
            }
        }


        if(canRemove)
        {
            const removeButton = document.createElement("button");

            removeButton.textContent = "Remove";


            removeButton.onclick = function()
            {
                removeRoomMember(member.id);
            };


            item.appendChild(removeButton);
        }


        memberList.appendChild(item);
    }
}



async function removeRoomMember(userId)
{
    const response = await roomRequest("/api/rooms/" + currentRoomId + "/members/" + userId, "DELETE");


    if(response && response.ok)
    {
        showRoomMessage("Member removed.");

        loadRoomMembers();
        loadRooms();
    }
}




async function leaveCurrentRoom()
{
    if(!currentRoom)
    {
        return;
    }


    if(currentRoom.currentUserCreator)
    {
        return;
    }


    const response = await roomRequest("/api/rooms/" + currentRoomId + "/leave", "DELETE");


    if(response && response.ok)
    {
        resetCurrentRoom();

        loadRooms();

        showRoomMessage("You left the room.");
    }
}




async function deleteCurrentRoom()
{
    let currentUser = null;

    const savedUser = sessionStorage.getItem("user");

    if(savedUser)
    {
        currentUser = JSON.parse(savedUser);
    }


    if(!currentRoom)
    {
        return;
    }


    if(!currentUser)
    {
        return;
    }


    let canDelete = false;


    if(currentUser.role === "MANAGER")
    {
        canDelete = true;
    }


    if(currentRoom.currentUserCreator)
    {
        canDelete = true;
    }


    if(!canDelete)
    {
        return;
    }


    const confirmed = confirm("Delete this room and its messages?");


    if(!confirmed)
    {
        return;
    }


    const response = await roomRequest("/api/rooms/" + currentRoomId, "DELETE");


    if(response && response.ok)
    {
        resetCurrentRoom();

        loadRooms();

        showRoomMessage("Room deleted.");
    }
}





async function deleteRoomById(roomId)
{
    const confirmed = confirm("Delete this room and its messages?");


    if(!confirmed)
    {
        return;
    }


    const response = await roomRequest( "/api/rooms/" + roomId, "DELETE");


    if(response && response.ok)
    {
        if(Number(currentRoomId) === Number(roomId))
        {
            resetCurrentRoom();
        }


        loadRooms();

        showRoomMessage("Room deleted.");
    }
}





function applyRoomActions()
{
    let currentUser = null;

    const savedUser = sessionStorage.getItem("user");

    if(savedUser)
    {
        currentUser = JSON.parse(savedUser);
    }


    const addButton = document.querySelector(".add_btn");
    const leaveButton = document.getElementById("leaveRoomButton");
    const deleteButton = document.getElementById("deleteRoomButton");
    const chatInputArea = document.getElementById("chatInputArea");
    const roomStats = document.querySelector(".room-stats");
    const roomMembers = document.querySelector(".room-members");


    let hasActiveRoom = false;

    if(currentRoom && currentRoom.currentUserMember)
    {
        hasActiveRoom = true;
    }


    if(chatInputArea)
    {
        if(hasActiveRoom)
        {
            chatInputArea.classList.remove("room-chat-disabled");
        }
        else
        {
            chatInputArea.classList.add("room-chat-disabled");
        }
    }


    if(roomStats)
    {
        if(hasActiveRoom)
        {
            roomStats.classList.remove("room-context-disabled");
        }
        else
        {
            roomStats.classList.add("room-context-disabled");
        }
    }


    if(roomMembers)
    {
        if(hasActiveRoom)
        {
            roomMembers.classList.remove("room-context-disabled");
        }
        else
        {
            roomMembers.classList.add("room-context-disabled");
        }
    }


    if(addButton)
    {
        addButton.style.display = "none";

        if(currentRoom && currentRoom.currentUserCreator)
        {
            addButton.style.display = "";
        }
    }


    if(leaveButton)
    {
        leaveButton.style.display = "none";

        if(currentRoom && !currentRoom.currentUserCreator)
        {
            leaveButton.style.display = "";
        }
    }


    if(deleteButton)
    {
        deleteButton.style.display = "none";


        if(currentRoom && currentUser)
        {
            if(currentUser.role === "MANAGER")
            {
                deleteButton.style.display = "";
            }
            else if(currentRoom.currentUserCreator)
            {
                deleteButton.style.display = "";
            }
        }
    }
}




function resetCurrentRoom()
{
    if(currentSubscription != null)
    {
        currentSubscription.unsubscribe();

        currentSubscription = null;
    }


    currentRoom = null;

    currentRoomId = null;

    currentRoomMessages.clear();


    document.getElementById("roomTitle").textContent = "Select a room";
    document.getElementById("activeRoomName").textContent = "No room selected";
    document.getElementById("chatBox").innerHTML = '<p class="room-empty-state">Select one of your rooms to start chatting.</p>';
    document.getElementById("memberList").innerHTML = "<li>No members</li>";
    document.getElementById("msgCount").textContent = "0";


    applyRoomActions();
}



async function roomRequest(path, method, body)
{
    const token = sessionStorage.getItem("token");


    const headers = 
    {
        "Authorization": "Bearer " + token
    };


    const options = 
    {
        method: method,
        headers: headers
    };


    if(body)
    {
        headers["Content-Type"] = "application/json";

        options.body = JSON.stringify(body);
    }


    try
    {
        const response = await fetch
        (
            "http://localhost:8080" + path,
            options
        );


        if(!response.ok)
        {
            const errorText = await response.text();


            if(errorText)
            {
                showRoomMessage(errorText);
            }
            else
            {
                showRoomMessage("Room request failed.");
            }
        }


        return response;
    }
    catch(error)
    {
        showRoomMessage("Cannot connect to the server.");

        return null;
    }
}




function showRoomMessage(message)
{
    if(typeof showNotificationToast === "function")
    {
        showNotificationToast(message);
    }
    else
    {
        alert(message);
    }
}




document.addEventListener("DOMContentLoaded",
    function()
    {
        let currentUser = null;

        const savedUser = sessionStorage.getItem("user");

        if(savedUser)
        {
            currentUser = JSON.parse(savedUser);
        }


        const addRoomButton = document.querySelector(".add-room");


        if(addRoomButton)
        {
            addRoomButton.style.display = "none";


            if(currentUser)
            {
                if(currentUser.role === "MANAGER" || currentUser.role === "TEAM_LEADER")
                {
                    addRoomButton.style.display = "";
                }
            }
        }


        applyRoomActions();


        const messageInput = document.getElementById("msgInput");


        if(messageInput)
        {
            messageInput.addEventListener("keydown",
                function(event)
                {
                    if(event.key === "Enter")
                    {
                        sendMessage(event);
                    }
                }
            );
        }


        connectWebSocket().catch(function(error){});

        loadRooms();
    }
);
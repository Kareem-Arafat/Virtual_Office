document.addEventListener("DOMContentLoaded", function()
{
    ensureChatbotUi();

    const launcher = document.getElementById("socum-ai-launcher");
    const chatWindow = document.getElementById("socum-ai-window");
    const closeButton = document.getElementById("socum-close-chat");
    const sendButton = document.getElementById("socum-send-btn");
    const input = document.getElementById("socum-chat-input");


    if(launcher)
    {
        launcher.addEventListener("click", function()
        {
            chatWindow.classList.remove("socum-hidden");

            const chatBody = document.getElementById("socum-chat-body");

            if(chatBody && chatBody.children.length === 0)
            {
                addAiMessage("Hello! I am RafiQ AI. How can I help you with your tasks or workspace?");
            }
        });
    }


    if (closeButton)
    {
        closeButton.addEventListener
        (
            "click",
            function()
            {
                chatWindow.classList.add("socum-hidden");
            }
        );
    }


    if (sendButton)
    {
        sendButton.addEventListener
        (
            "click",
            sendAiMessage
        );
    }


    if (input)
    {
        input.addEventListener
        (
            "keydown",
            function(event)
            {
                if (event.key === "Enter")
                {
                    sendAiMessage();
                }
            }
        );
    }
});


function ensureChatbotUi()
{
    if(document.getElementById("socum-ai-launcher"))
    {
        return;
    }

    const chatbot = document.createElement("div");
    chatbot.innerHTML = `
        <div id="socum-ai-launcher" class="socum-chat-launcher">
            <img src="https://cdn-icons-png.flaticon.com/512/4712/4712035.png" alt="AI Bot">
        </div>

        <div id="socum-ai-window" class="socum-chat-window socum-hidden">
            <div class="socum-chat-header">
                <div class="socum-bot-info">
                    <span class="socum-status-dot"></span>
                    <span>RafiQ AI Assistant</span>
                </div>
                <button id="socum-close-chat" class="close_button">&times;</button>
            </div>

            <div id="socum-chat-body" class="socum-chat-body"></div>

            <div class="socum-chat-footer">
                <input type="text" id="socum-chat-input" placeholder="Ask about your tasks or workspace">
                <button id="socum-send-btn">➤</button>
            </div>
        </div>
    `;

    while(chatbot.firstElementChild)
    {
        document.body.appendChild(chatbot.firstElementChild);
    }
}




async function sendAiMessage()
{
    const token = sessionStorage.getItem("token");


    const input = document.getElementById("socum-chat-input");
    const chatBody = document.getElementById("socum-chat-body");
    const prompt = input.value.trim();


    if (prompt === "")
    {
        return;
    }

    addUserMessage(prompt);


    input.value = "";


    const loadingMessage = document.createElement("div");

    loadingMessage.className = "ai-message";
    loadingMessage.textContent = "RafiQ is thinking...";

    chatBody.appendChild(loadingMessage);


    chatBody.scrollTop = chatBody.scrollHeight;


    try
    {
        const response = await fetch
        (
            "http://localhost:8080/api/ai/ask",
            {
                method: "POST",

                headers:
                {
                    "Content-Type": "application/json",

                    ...(token? { "Authorization": "Bearer " + token }: {})
                },

                body: JSON.stringify(
                {
                    prompt: prompt
                })
            }
        );


        if (response.status === 401)
        {
            sessionStorage.removeItem("token");

            window.location.href = "login.html";

            return;
        }


        if (!response.ok)
        {
            loadingMessage.textContent = "Failed to get response from RafiQ.";

            return;
        }


        const data = await response.json();


        loadingMessage.textContent = data.response;

        chatBody.scrollTop = chatBody.scrollHeight;
    }
    catch (error)
    {

        loadingMessage.textContent = "Cannot connect to RafiQ AI right now.";
    }
}




function addUserMessage(message)
{
    const chatBody = document.getElementById("socum-chat-body");
    const messageDiv = document.createElement("div");


    messageDiv.className = "user-message";
    messageDiv.textContent = message;

    chatBody.appendChild(messageDiv);

    chatBody.scrollTop = chatBody.scrollHeight;
}




function addAiMessage(message)
{
    const chatBody = document.getElementById("socum-chat-body");
    const messageDiv = document.createElement("div");

    messageDiv.className = "ai-message";


    messageDiv.textContent = message;


    chatBody.appendChild(messageDiv);


    chatBody.scrollTop =chatBody.scrollHeight;
}

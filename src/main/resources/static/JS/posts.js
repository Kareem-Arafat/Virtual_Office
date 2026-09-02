const POSTS_API = "http://localhost:8080/api/posts";



function previewPostMedia(input)
{
    const file = input.files[0];
    const preview = document.getElementById("postMediaPreview");
    const image = document.getElementById("postImgPreview");
    const video = document.getElementById("postVidPreview");

    if(!file || !preview || !image || !video)
    {
        return;
    }

    const allowedTypes = 
    [
        "image/jpeg",
        "image/png",
        "image/gif",
        "image/webp",
        "video/mp4",
        "video/webm"
    ];


    let maximumSize = 5 * 1024 * 1024;

    if(file.type.startsWith("video/"))
    {
        maximumSize = 15 * 1024 * 1024;
    }


    if(!allowedTypes.includes(file.type) || file.size > maximumSize)
    {
        input.value = "";
        alert("Choose a supported image up to 5 MB or video up to 15 MB.");

        return;
    }


    const url = URL.createObjectURL(file);


    if(file.type.startsWith("image/"))
    {
        image.style.display = "block";
        video.style.display = "none";

        image.src = url;
    }
    else if(file.type.startsWith("video/"))
    {
        image.style.display = "none";
        video.style.display = "block";

        video.src = url;
    }


    preview.style.display = "block";

}

function clearPostMedia()
{
    const input = document.getElementById("postMediaInput");
    const preview = document.getElementById("postMediaPreview");
    const image = document.getElementById("postImgPreview");
    const video = document.getElementById("postVidPreview");

    if(input) 
    {
        input.value = "";
    }

    if(image) 
    { 
        image.removeAttribute("src"); 
        image.style.display = "none"; 
    }

    if(video) 
    {
        video.removeAttribute("src"); 
        video.style.display = "none"; 
    }

    if(preview)
    {
        preview.style.display = "none";
    }
}




async function addPost()
{
    const token = sessionStorage.getItem("token");

    if(!token)
    {
        window.location.href = "login.html";
        return;
    }


    const postInput = document.getElementById("postInput");
    const imageInput = document.getElementById("postMediaInput");

    const content = postInput.value.trim();

    let file;

    if(imageInput)
    {
        file = imageInput.files[0];
    }


    if(content === "" && !file)
    {
        return;
    }


    let imageUrl = null;

    if(file)
    {
        imageUrl = await new Promise(function(resolve, reject)
        {
            const reader = new FileReader();

            reader.onload = function()
            {
                resolve(reader.result);
            };

            reader.onerror = function()
            {
                reject(reader.error);
            };

            reader.readAsDataURL(file);
        });
    }


    const response = await fetch
    (
        "http://localhost:8080/api/posts",
        {
            method: "POST",

            headers:
            {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + token
            },

            body: JSON.stringify(
            {
                content: content,
                imageUrl: imageUrl
            })
        }
    );


    if(!response.ok)
    {
        return;
    }


    postInput.value = "";

    if(imageInput)
    {
        imageInput.value = "";
    }

    const preview = document.getElementById("postMediaPreview");

    if(preview)
    {
        preview.style.display = "none";
    }


    await loadPosts();
}





async function loadPosts()
{
    const token = sessionStorage.getItem("token");

    if(!token)
    {
        return;
    }


    const response = await fetch
    (
        POSTS_API,
        {
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


    const posts = await response.json();

    displayPosts(posts);
}





function displayPosts(posts)
{
    const list = document.getElementById("postList");

    if(!list)
    {
        return;
    }


    list.innerHTML = "";


    for(const post of posts)
    {
        const card = document.createElement("div");
        card.className = "post-card";

        let commentsHtml = "";

        let comments = [];

        if(post.comments)
        {
            comments = post.comments;
        }


        for(const comment of comments)
        {
            let deleteCommentButton = "";

            if(comment.canDelete)
            {
                deleteCommentButton = `
                    <button
                        type="button"
                        onclick="deleteComment(${post.id}, ${comment.id})"
                    >
                        Delete
                    </button>
                `;
            }


            commentsHtml += `
                <div class="post-comment-item">

                    <div>
                        <strong>
                            ${escapeText(comment.username)}
                        </strong>

                        <span>
                            ${escapeText(comment.text)}
                        </span>
                    </div>

                    ${deleteCommentButton}

                </div>
            `;
        }


        let deletePostButton = "";

        if(post.canDelete)
        {
            deletePostButton = `
                <button
                    type="button"
                    onclick="deletePost(${post.id})"
                >
                    Delete
                </button>
            `;
        }


        let likeIcon = "🤍";

        if(post.likedByMe)
        {
            likeIcon = "❤️";
        }


        card.innerHTML = `
            <div class="post-card-header">

                <h3>
                    ${escapeText(post.authorUsername)}
                </h3>

                ${deletePostButton}

            </div>


            <p>
                ${escapeText(post.content)}
            </p>


            ${getPostMediaHtml(post.imageUrl)}


            <small>
                ${formatDate(post.createdAt)}
            </small>


            <div>
                <button
                    type="button"
                    onclick="toggleLike(${post.id})"
                >
                    ${likeIcon}
                    ${post.likesCount}
                </button>
            </div>


            <div class="post-comments">
                ${commentsHtml}
            </div>


            <div class="comment-box">

                <input
                    type="text"
                    id="commentInput-${post.id}"
                    placeholder="Write a comment..."
                    onkeydown="
                        if(event.key === 'Enter')
                        {
                            addComment(${post.id});
                        }
                    "
                >

                <button
                    type="button"
                    onclick="addComment(${post.id})"
                >
                    Comment
                </button>

            </div>
        `;


        list.appendChild(card);
    }
}





async function toggleLike(postId)
{
    const token = sessionStorage.getItem("token");

    const response = await fetch
    (
        POSTS_API + "/" + postId + "/like",
        {
            method: "POST",

            headers:
            {
                "Authorization": "Bearer " + token
            }
        }
    );


    if(response.ok)
    {
        await loadPosts();
    }
}





async function addComment(postId)
{
    const token = sessionStorage.getItem("token");
    const input = document.getElementById("commentInput-" + postId);
    const text = input.value.trim();


    if(text === "")
    {
        return;
    }


    const response = await fetch
    (
        POSTS_API + "/" + postId + "/comments",
        {
            method: "POST",

            headers:
            {
                "Content-Type": "application/json",

                "Authorization": "Bearer " + token
            },

            body: JSON.stringify(
            {
                text: text
            })
        }
    );


    if(!response.ok)
    {
        return;
    }


    input.value = "";

    await loadPosts();
}





async function deleteComment(postId, commentId)
{
    const token = sessionStorage.getItem("token");

    const response = await fetch
    (
        POSTS_API + "/" + postId + "/comments/" + commentId,
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


    await loadPosts();
}





async function deletePost(postId)
{
    const token = sessionStorage.getItem("token");


    const response = await fetch
    (
        POSTS_API + "/" + postId,
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


    await loadPosts();
}





function formatDate(date)
{
    if(!date)
    {
        return "";
    }

    return new Date(date).toLocaleString();
}




function escapeText(text)
{
    const div = document.createElement("div");

    if(!text)
    {
        div.textContent = "";
    }
    else
    {
        div.textContent = text;
    }

    return div.innerHTML;
}





function getPostMediaHtml(mediaUrl)
{
    if(typeof mediaUrl !== "string")
    {
        return "";
    }


    const isImage = mediaUrl.startsWith("data:image/") || mediaUrl.startsWith("http://") || mediaUrl.startsWith("https://");

    const isVideo =
        mediaUrl.startsWith("data:video/");


    if(!isImage && !isVideo)
    {
        return "";
    }


    const safeUrl = escapeText(mediaUrl);


    if(isVideo)
    {
        return `
            <video
                controls
                preload="metadata"
                src="${safeUrl}"
                style="max-width:100%; border-radius:10px;"
            >
            </video>
        `;
    }


    return `
        <img
            src="${safeUrl}"
            alt="Post media"
            loading="lazy"
            style="max-width:100%; border-radius:10px;"
        >
    `;
}



document.addEventListener("DOMContentLoaded",loadPosts);

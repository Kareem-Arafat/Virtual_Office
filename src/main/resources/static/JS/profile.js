const API = "http://localhost:8080";




async function loadProfile()
{
    const token = sessionStorage.getItem("token");

    if(!token)
    {
        window.location.href = "login.html";
        return null;
    }

    const response = await fetch(API + "/me",
    {
        headers:
        {
            "Authorization": "Bearer " + token
        }
    });

    if(!response.ok)
    {
        return null;
    }

    const user = await response.json();

    sessionStorage.setItem("user", JSON.stringify(user));

    document.getElementById("nameInput").value = user.username || "";
    document.getElementById("emailInput").value = user.email || "";
    document.getElementById("phoneInput").value = user.phone || "";
    document.getElementById("staffIdDisplay").value = user.staffId || "—";

    document.getElementById("displayName").textContent = user.username || "";
    document.getElementById("introRole").textContent = user.role || "";
    document.getElementById("introStaffId").textContent = "Staff ID: " + (user.staffId || "—");

    return user;
}



async function saveProfile()
{
    const token = sessionStorage.getItem("token");
    const currentUser = JSON.parse(sessionStorage.getItem("user") || "null");

    const response = await fetch(API + "/api/users/profile",
    {
        method: "PUT",

        headers:
        {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + token
        },

        body: JSON.stringify(
        {
            username: document.getElementById("nameInput").value.trim(),
            email: document.getElementById("emailInput").value.trim(),
            phone: document.getElementById("phoneInput").value.trim(),
            bio: document.getElementById("bioInput").value.trim()
        })
    });

    if(!response.ok)
    {
        return;
    }

    const user = await response.json();

    if(currentUser && currentUser.username !== user.username)
    {
        sessionStorage.removeItem("token");
        sessionStorage.removeItem("user");
        alert("Username updated. Please log in again.");
        window.location.href = "login.html";
        return;
    }

    sessionStorage.setItem("user", JSON.stringify(user));

    document.getElementById("displayName").textContent = user.username;

    if(typeof loadNavbarUser === "function")
    {
        loadNavbarUser();
    }

    alert("Profile saved");
}





function saveProfileExtended()
{
    const user = JSON.parse(sessionStorage.getItem("user") || "null");

    if(!user)
    {
        return;
    }

    const skills = document
        .getElementById("skillsInput")
        .value
        .split(",")
        .map(skill => skill.trim())
        .filter(Boolean);


    const experience = [];

    for(let i = 1; i <= 3; i++)
    {
        const role = document.getElementById("exp" + i + "Role").value.trim();
        const years = document.getElementById("exp" + i + "Years").value.trim();

        if(role || years)
        {
            experience.push(
            {
                role: role,
                years: years
            });
        }
    }


    const data =
    {
        bio: document.getElementById("bioInput").value.trim(),
        skills: skills,
        experience: experience
    };


    localStorage.setItem("profile_about_" + user.id,JSON.stringify(data));


    showAbout(data);
    saveProfile();
}





function loadAbout()
{
    const user = JSON.parse(sessionStorage.getItem("user") || "null");

    if(!user)
    {
        return;
    }


    const saved = JSON.parse(localStorage.getItem("profile_about_" + user.id) || "{}");


    const data =
    {
        bio: saved.bio || user.bio || "",
        skills: saved.skills || [],
        experience: saved.experience || []
    };


    document.getElementById("bioInput").value = data.bio;
    document.getElementById("skillsInput").value = data.skills.join(", ");


    for(let i = 0; i < 3; i++)
    {
        document.getElementById("exp" + (i + 1) + "Role").value = data.experience[i]?.role || "";
        document.getElementById("exp" + (i + 1) + "Years").value = data.experience[i]?.years || "";
    }


    showAbout(data);
}




function showAbout(data)
{
    document.getElementById("profileBio").textContent = data.bio || "No bio yet";

    const skillsBox = document.getElementById("profileSkillsDisplay");

    if(data.skills.length === 0)
    {
        skillsBox.textContent = "No skills added yet.";
    }
    else
    {
        skillsBox.replaceChildren();

        for(const skill of data.skills)
        {
            const skillElement = document.createElement("span");
            skillElement.className = "skill-pill";
            skillElement.textContent = skill;
            skillsBox.appendChild(skillElement);
        }
    }


    const experienceBox = document.getElementById("profileExpDisplay");

    if(data.experience.length === 0)
    {
        experienceBox.textContent = "No experience added yet.";
    }
    else
    {
        experienceBox.replaceChildren();

        for(const item of data.experience)
        {
            const entry = document.createElement("div");
            const role = document.createElement("strong");
            const years = document.createElement("span");

            entry.className = "exp-entry";
            role.textContent = item.role;
            years.textContent = item.years;
            entry.append(role, years);
            experienceBox.appendChild(entry);
        }
    }
}





function fixLink(link)
{
    link = link.trim();

    if(!link)
    {
        return "";
    }

    if(!link.startsWith("http://") && !link.startsWith("https://"))
    {
        link = "https://" + link;
    }

    return link;
}





function saveSocialLinks()
{
    const user = JSON.parse(sessionStorage.getItem("user") || "null");

    if(!user)
    {
        return;
    }


    const links =
    {
        github: fixLink(document.getElementById("linkGithubInput").value),
        twitter: fixLink(document.getElementById("linkTwitterInput").value),
        linkedin: fixLink(document.getElementById("linkLinkedinInput").value)
    };


    localStorage.setItem("profile_links_" + user.id,JSON.stringify(links));


    showLinks(links);

    alert("Links saved");
}





function loadSocialLinks()
{
    const user = JSON.parse(sessionStorage.getItem("user") || "null");

    if(!user)
    {
        return;
    }


    const links = JSON.parse(localStorage.getItem("profile_links_" + user.id) || "{}");


    document.getElementById("linkGithubInput").value = links.github || "";
    document.getElementById("linkTwitterInput").value = links.twitter || "";
    document.getElementById("linkLinkedinInput").value = links.linkedin || "";

    showLinks(links);
}




function showLinks(links)
{
    document.getElementById("linkGithub").href = links.github || "#";
    document.getElementById("linkTwitter").href = links.twitter || "#";
    document.getElementById("linkLinkedin").href = links.linkedin || "#";
}





function setupProfilePhoto()
{
    const user = JSON.parse(sessionStorage.getItem("user") || "null");

    if(!user)
    {
        return;
    }


    const camera = document.getElementById("cameraBtn");
    const menu = document.getElementById("uploadMenu");

    const choose = document.getElementById("chooseProfile");
    const remove = document.getElementById("removeProfile");

    const input = document.getElementById("imageInput");

    const profileImage = document.getElementById("profileImage");
    const navImage = document.getElementById("navUserAvatar");


    const key = "profile_photo_" + user.id;

    const savedPhoto = localStorage.getItem(key);


    if(savedPhoto)
    {
        profileImage.src = savedPhoto;

        if(navImage)
        {
            navImage.src = savedPhoto;
        }
    }


    camera.onclick = function()
    {
        if(menu.style.display === "block")
        {
            menu.style.display = "none";
        }
        else
        {
            menu.style.display = "block";
        }
    };


    choose.onclick = function()
    {
        input.click();
        menu.style.display = "none";
    };


    input.onchange = function()
    {
        const file = input.files[0];

        if(!file)
        {
            return;
        }


        const reader = new FileReader();


        reader.onload = function()
        {
            localStorage.setItem(key, reader.result);

            profileImage.src = reader.result;

            if(navImage)
            {
                navImage.src = reader.result;
            }
        };


        reader.readAsDataURL(file);
    };


    remove.onclick = function()
    {
        localStorage.removeItem(key);

        profileImage.src = "images/default.png";

        if(navImage)
        {
            navImage.src = "images/default.png";
        }

        menu.style.display = "none";
    };
}




async function loadTaskStats(user)
{
    const box = document.getElementById("completedTasksCount");

    if(!box || !user)
    {
        return;
    }


    if(user.role === "MANAGER")
    {
        box.closest(".pc-stat").style.display = "none";
        return;
    }


    const token = sessionStorage.getItem("token");


    const response = await fetch(API + "/api/tasks",
    {
        headers:
        {
            "Authorization": "Bearer " + token
        }
    });


    if(!response.ok)
    {
        box.textContent = "0";
        return;
    }


    const tasks = await response.json();


    const done = tasks.filter(task => task.status === "DONE").length;


    box.textContent = done;


    const score = document.getElementById("activityScore");

    if(score)
    {
        if(tasks.length === 0)
        {
            score.textContent = "0%";
        }
        else
        {
            score.textContent = Math.round(done / tasks.length * 100) + "%";
        }
    }
}




async function loadRoomCount()
{
    const token = sessionStorage.getItem("token");

    const box = document.getElementById("teamRoomsCount");


    if(!box)
    {
        return;
    }


    const response = await fetch(API + "/api/rooms",
    {
        headers:
        {
            "Authorization": "Bearer " + token
        }
    });


    if(!response.ok)
    {
        box.textContent = "0";
        return;
    }


    const rooms = await response.json();

    box.textContent = rooms.length;
}




function switchTab(tabId, button)
{
    document.querySelectorAll(".tab-panel").forEach(tab =>
    {
        tab.classList.remove("active");
    });


    document.querySelectorAll(".profile-tabs button").forEach(btn =>
    {
        btn.classList.remove("active");
    });


    document.getElementById(tabId).classList.add("active");

    button.classList.add("active");
}




document.addEventListener("DOMContentLoaded", async function()
{
    const user = await loadProfile();

    if(!user)
    {
        return;
    }

    loadAbout();
    loadSocialLinks();
    setupProfilePhoto();

    await loadTaskStats(user);
    await loadRoomCount();
});

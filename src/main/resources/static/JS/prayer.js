let prayerTimes = {};
let countdownInterval = null;
let lastAlertedPrayer = null;




document.addEventListener("DOMContentLoaded", function()
{
    setupPrayerWidget();
    getUserLocationAndLoadPrayerTimes();
});




function setupPrayerWidget()
{
    const prayerBtn = document.getElementById("prayerBtn");
    const prayerTab = document.getElementById("prayerTab");
    const closeAlertBtn = document.getElementById("closeAlertBtn");

    if(prayerBtn && prayerTab)
    {
        prayerBtn.addEventListener("click", function()
        {
            prayerTab.classList.toggle("active");
        });
    }

    if(closeAlertBtn)
    {
        closeAlertBtn.addEventListener("click", closePrayerAlert);
    }
}





function getUserLocationAndLoadPrayerTimes()
{
    if(!navigator.geolocation)
    {
        return;
    }

    navigator.geolocation.getCurrentPosition
    (
        function(position)
        {
            const latitude = position.coords.latitude;
            const longitude = position.coords.longitude;

            loadPrayerTimes(latitude, longitude);
        },

        function(error)
        {

            const nextPrayerName = document.getElementById("nextPrayerName");

            if(nextPrayerName)
            {
                nextPrayerName.textContent = "Location required";
            }
        }
    );
}





async function loadPrayerTimes(latitude, longitude)
{
    const token = sessionStorage.getItem("token");

    if(!token)
    {
        return;
    }

    try
    {
        const response = await fetch
        (
            `http://localhost:8080/api/prayer-times?lat=${latitude}&lon=${longitude}`,
            {
                method: "GET",

                headers:
                {
                    "Authorization": "Bearer " + token
                }
            }
        );

        if(response.status === 401)
        {
            sessionStorage.removeItem("token");
            window.location.href = "login.html";
            return;
        }

        if(!response.ok)
        {
            return;
        }

        prayerTimes = await response.json();

        displayPrayerTimes();
        startPrayerCountdown();
    }
    catch(error){}
}




function displayPrayerTimes()
{
    setPrayerTime("Fajr", prayerTimes.Fajr);
    setPrayerTime("Sunrise", prayerTimes.Sunrise);
    setPrayerTime("Dhuhr", prayerTimes.Dhuhr);
    setPrayerTime("Asr", prayerTimes.Asr);
    setPrayerTime("Maghrib", prayerTimes.Maghrib);
    setPrayerTime("Isha", prayerTimes.Isha);

    const hijriDay = document.getElementById("hijriDay");
    const hijriMonth = document.getElementById("hijriMonth");

    if(hijriDay)
    {
        hijriDay.textContent = prayerTimes.hijriDay;
    }

    if(hijriMonth)
    {
        hijriMonth.textContent = prayerTimes.hijriMonth;
    }
}



function setPrayerTime(prayerName, time)
{
    const element = document.getElementById("time-" + prayerName);

    if(element && time)
    {
        element.textContent = cleanPrayerTime(time);
    }
}



function cleanPrayerTime(time)
{
    if(!time)
    {
        return "--:--";
    }

    return time.split(" ")[0].substring(0, 5);
}





function startPrayerCountdown()
{
    if(countdownInterval)
    {
        clearInterval(countdownInterval);
    }

    updateNextPrayer();

    countdownInterval = setInterval(updateNextPrayer, 1000);
}





function updateNextPrayer()
{
    if(!prayerTimes.Fajr)
    {
        return;
    }

    const prayers =
    [
        {
            name: "Fajr",
            arabicName: "الفجر",
            time: prayerTimes.Fajr
        },

        {
            name: "Dhuhr",
            arabicName: "الظهر",
            time: prayerTimes.Dhuhr
        },

        {
            name: "Asr",
            arabicName: "العصر",
            time: prayerTimes.Asr
        },

        {
            name: "Maghrib",
            arabicName: "المغرب",
            time: prayerTimes.Maghrib
        },

        {
            name: "Isha",
            arabicName: "العشاء",
            time: prayerTimes.Isha
        }
    ];

    const now = new Date();

    let nextPrayer = null;
    let nextPrayerDate = null;

    for(const prayer of prayers)
    {
        const prayerDate = createPrayerDate(prayer.time);

        if(prayerDate > now)
        {
            nextPrayer = prayer;
            nextPrayerDate = prayerDate;
            break;
        }
    }


    if(nextPrayer === null)
    {
        nextPrayer = prayers[0];

        nextPrayerDate = createPrayerDate(prayerTimes.Fajr);

        nextPrayerDate.setDate(nextPrayerDate.getDate() + 1);
    }

    updateNextPrayerUI(nextPrayer, nextPrayerDate);

    updateActivePrayer(nextPrayer.name);

    checkPrayerAlert(prayers, now);
}





function createPrayerDate(time)
{
    const cleanTime = cleanPrayerTime(time);

    const [hours, minutes] = cleanTime.split(":").map(Number);

    const date = new Date();

    date.setHours(hours, minutes, 0, 0);

    return date;
}





function updateNextPrayerUI(prayer, prayerDate)
{
    const nextPrayerName = document.getElementById("nextPrayerName");

    const nextPrayerTime = document.getElementById("nextPrayerTime");

    const timeLeft = document.getElementById("nextPrayerTimeLeft");

    if(nextPrayerName)
    {
        nextPrayerName.textContent = prayer.arabicName;
    }

    if(nextPrayerTime)
    {
        nextPrayerTime.textContent = cleanPrayerTime(prayer.time);
    }

    let difference = prayerDate.getTime() - Date.now();

    if(difference < 0)
    {
        difference = 0;
    }

    const totalSeconds = Math.floor(difference / 1000);
    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;

    if(timeLeft)
    {
        timeLeft.textContent = `${padNumber(hours)}:${padNumber(minutes)}:${padNumber(seconds)}`;
    }
}




function padNumber(number)
{
    return String(number).padStart(2, "0");
}




function updateActivePrayer(nextPrayerName)
{
    const prayerNames =
    [
        "Fajr",
        "Sunrise",
        "Dhuhr",
        "Asr",
        "Maghrib",
        "Isha"
    ];

    for(const name of prayerNames)
    {
        const item = document.getElementById("item-" + name);

        if(item)
        {
            item.classList.remove("active");
        }
    }

    const nextItem = document.getElementById("item-" + nextPrayerName);

    if(nextItem)
    {
        nextItem.classList.add("active");
    }
}





function checkPrayerAlert(prayers, now)
{
    for(const prayer of prayers)
    {
        const prayerDate = createPrayerDate(prayer.time);
        const difference = Math.abs(now.getTime() - prayerDate.getTime());


        if(difference <= 2000)
        {
            const alertKey = now.toDateString() +  "-" +  prayer.name;

            if(lastAlertedPrayer !== alertKey)
            {
                lastAlertedPrayer = alertKey;
                showPrayerAlert(prayer.arabicName);
            }
        }
    }
}





function showPrayerAlert(arabicPrayerName)
{
    const alertBox = document.getElementById("customPrayerAlert");

    const alertName = document.getElementById("alertPrayerNameAr");

    const audio = document.getElementById("prayerAudio");

    if(alertName)
    {
        alertName.textContent = "أذان " + arabicPrayerName;
    }

    if(alertBox)
    {
        alertBox.classList.add("show");
    }

    if(audio)
    {
        audio.currentTime = 0;

        audio.play().catch(function(error){});
    }
}





function closePrayerAlert()
{
    const alertBox = document.getElementById("customPrayerAlert");
    const audio = document.getElementById("prayerAudio");

    if(alertBox)
    {
        alertBox.classList.remove("show");
    }

    if(audio)
    {
        audio.pause();
        audio.currentTime = 0;
    }
}
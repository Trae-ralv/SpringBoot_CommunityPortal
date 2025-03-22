document.addEventListener('DOMContentLoaded', function() {
    let timeLeft = 15;
    const countdownElement = document.getElementById('countdown');
    const progressBar = document.getElementById('progressBar');

    // Update countdown and progress bar every second
    const timer = setInterval(function() {
        timeLeft--;
        countdownElement.textContent = timeLeft;

        // Update progress bar width (percentage)
        const progress = (timeLeft / 15) * 100;
        progressBar.style.width = progress + '%';
        progressBar.setAttribute('aria-valuenow', progress);

        // Redirect when time is up
        if (timeLeft <= 0) {
            clearInterval(timer);
            window.location.href = '/';
        }
    }, 1000);
});
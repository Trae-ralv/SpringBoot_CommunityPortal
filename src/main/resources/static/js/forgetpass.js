$(document).ready(function() {
    if ($('#error-alert').length) {
        $('#error-alert').addClass('show');
        // Hide alert after 15 seconds
        setTimeout(function() {
            $('#error-alert').removeClass('show');
        }, 15000);
    }
});

function validateForm() {
    const email = document.getElementById('email').value;
    const isEmailValid = validateEmail(email);

    const isValid = isEmailValid;

    const submitContainer = document.getElementById('submit-container');
    const submitButton = document.getElementById('submit-button');
}

document.getElementById('email').addEventListener('input', function () {
    const email = this.value;
    const errorElement = document.getElementById('email_error');
    if (!validateEmail(email)) {
        this.classList.add('invalid');
        errorElement.textContent = 'Please enter a valid email address (e.g., example@domain.com).';
    } else {
        this.classList.remove('invalid');
        errorElement.textContent = '';
    }
    validateForm();
});


// Helper functions use in validateForm()
function validateEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(String(email).toLowerCase());
    // return with lowercase
}
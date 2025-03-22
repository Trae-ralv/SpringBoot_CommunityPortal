// Form validation function
function validateForm() {
    const f_name = document.getElementById('f_name').value;
    const l_name = document.getElementById('l_name').value;
    const email = document.getElementById('email').value;
    const phone_no = document.getElementById('phone_no').value;

    const isFNameValid = !/[0-9]/.test(f_name) && f_name.trim() !== '';
    const isLNameValid = !/[0-9]/.test(l_name) && l_name.trim() !== '';
    const isEmailValid = validateEmail(email);
    const isPhoneValid = validatePhone(phone_no) || phone_no.trim() === '';

    const isValid = isFNameValid && isLNameValid && isEmailValid && isPhoneValid;
    const submitButton = document.getElementById('submit-button');

    submitButton.textContent = isValid ? 'Save Changes' : 'Save Disabled';
    submitButton.disabled = !isValid;

    return isValid;
}

// Input validation for first name
document.getElementById('f_name').addEventListener('input', function () {
    const value = this.value;
    const errorElement = document.getElementById('f_name_error');

    if (/[0-9]/.test(value)) {
        this.classList.add('invalid');
        errorElement.textContent = 'First Name cannot contain numbers';
    } else if (value.trim() === '') {
        this.classList.add('invalid');
        errorElement.textContent = 'First Name is required';
    } else {
        this.classList.remove('invalid');
        errorElement.textContent = '';
    }
    validateForm();
});

// Input validation for last name
document.getElementById('l_name').addEventListener('input', function () {
    const value = this.value;
    const errorElement = document.getElementById('l_name_error');

    if (/[0-9]/.test(value)) {
        this.classList.add('invalid');
        errorElement.textContent = 'Last Name cannot contain numbers';
    } else if (value.trim() === '') {
        this.classList.add('invalid');
        errorElement.textContent = 'Last Name is required';
    } else {
        this.classList.remove('invalid');
        errorElement.textContent = '';
    }
    validateForm();
});

// Input validation for email
document.getElementById('email').addEventListener('input', function () {
    const value = this.value;
    const errorElement = document.getElementById('email_error');

    if (!validateEmail(value)) {
        this.classList.add('invalid');
        errorElement.textContent = 'Please enter a valid email address';
    } else {
        this.classList.remove('invalid');
        errorElement.textContent = '';
    }
    validateForm();
});

// Input validation for phone number
document.getElementById('phone_no').addEventListener('input', function () {
    const value = this.value;
    const errorElement = document.getElementById('phone_no_error');

    if (value.trim() !== '' && !validatePhone(value)) {
        this.classList.add('invalid');
        errorElement.textContent = 'Please enter a valid phone number (e.g., 123-4567890)';
    } else {
        this.classList.remove('invalid');
        errorElement.textContent = '';
    }
    validateForm();
});

// Email validation regex
function validateEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(String(email).toLowerCase());
}

// Phone validation regex
function validatePhone(phone) {
    const re = /^[0-9]{3}-[0-9]{7,11}$/;
    return re.test(phone);
}

// Clear error messages and invalid classes
function clearErrors() {
    document.querySelectorAll('.error-message').forEach(error => error.textContent = '');
    document.querySelectorAll('.invalid').forEach(input => input.classList.remove('invalid'));
}

// Toggle edit mode for the form
function toggleEditMode(enable) {
    const inputs = document.querySelectorAll('.form-control');
    const viewModeButtons = document.getElementById('view-mode-buttons');
    const editModeButtons = document.getElementById('edit-mode-buttons');

    inputs.forEach(input => input.disabled = !enable);

    if (enable) {
        viewModeButtons.style.display = 'none';
        editModeButtons.style.display = 'block';
        clearErrors();
        validateForm();
    } else {
        viewModeButtons.style.display = 'block';
        editModeButtons.style.display = 'none';
        clearErrors();
    }
}

// Event listeners for edit and cancel buttons
document.getElementById('edit-profile-link').addEventListener('click', function (e) {
    e.preventDefault();
    toggleEditMode(true);
});

document.getElementById('cancel-link').addEventListener('click', function (e) {
    e.preventDefault();
    toggleEditMode(false);
    document.querySelector('form').reset();
});

// Hide alerts with fade-out animation after 15 seconds
document.addEventListener('DOMContentLoaded', function () {
    const successAlert = document.querySelector('.alert-success');
    const errorAlert = document.querySelector('.alert-danger');

    if (successAlert) {
        setTimeout(() => {
            successAlert.classList.add('fade-out');
            // Remove from DOM after animation completes (1s)
            setTimeout(() => successAlert.style.display = 'none', 1000);
        }, 5000); // 15-second delay before starting fade-out
    }

    if (errorAlert) {
        setTimeout(() => {
            errorAlert.classList.add('fade-out');
            // Remove from DOM after animation completes (1s)
            setTimeout(() => errorAlert.style.display = 'none', 1000);
        }, 5000); // 15-second delay before starting fade-out
    }
});
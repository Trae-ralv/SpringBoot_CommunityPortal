$(document).ready(function() {
    // Constants for DOM elements
    const $username = $('#username');
    const $password = $('#password');
    const $emailError = $('#email_error');
    const $passwordError = $('#password_error');
    const $submitContainer = $('#submit-container');
    const $submitButton = $('#submit-button');
    const $showPassword = $('#show-password');

    // Alert handling
    $('.alert-slide').each(function() {
        const $alert = $(this);
        $alert.addClass('show');
        setTimeout(() => $alert.removeClass('show'), 15000);
    });

    // Show/hide password toggle
    $showPassword.on('change', function() {
        const isChecked = $(this).is(':checked');
        $password.attr('type', isChecked ? 'text' : 'password');
        $password.trigger('input'); // Re-validate after toggle
    });

    // Form validation
    function validateForm() {
        const email = $username.val();
        const password = $password.val();
        const isEmailValid = validateEmail(email);
        const isPasswordValid = validatePassword(password);
        const isValid = isEmailValid && isPasswordValid;

        $submitContainer
            .toggleClass('text-start', !isValid)
            .toggleClass('text-end', isValid);
        $submitButton
            .text(isValid ? 'Login' : 'Login Disabled')
            .prop('disabled', !isValid);
    }

    // Email validation
    $username.on('input', function() {
        const email = this.value;
        const isValid = validateEmail(email);

        $(this).toggleClass('invalid', !isValid);
        $emailError.text(
            isValid ? '' : 'Please enter a valid email address (e.g., example@domain.com)'
        );
        validateForm();
    });

    // Password validation
    $password.on('input', function() {
        const password = this.value;
        const isValid = validatePassword(password);

        $(this).toggleClass('invalid', !isValid);
        $passwordError.text(
            isValid ? '' : 'Password must contain at least 8 characters, one uppercase letter, one lowercase letter, and one special character'
        );
        validateForm();
    });

    // Validation helpers
    function validateEmail(email) {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(String(email).toLowerCase());
    }

    function validatePassword(password) {
        const re = /^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*?,]).{8,}$/;
        return re.test(password);
    }

    // Initialize
    validateForm();
});
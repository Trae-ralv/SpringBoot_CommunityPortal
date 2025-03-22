$(document).ready(function () {
    // Password validation regex
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*?,]).{8,}$/;

    // Cache jQuery selectors
    const $password = $('#password');
    const $cPassword = $('#c_password');
    const $passwordError = $('#password_error');
    const $cPasswordError = $('#c_password_error');
    const $submitContainer = $('#submit-container');
    const $submitButton = $('#submit-button');
    const $showPassword = $('#show-password');
    const $showCPassword = $('#show-c-password');

    // Show/hide password toggles
    $showCPassword.on('change', function() {
        $password.attr('type', $(this).is(':checked') ? 'text' : 'password');
        $password.trigger('input');
    });

    $showCPassword.on('change', function() {
        $cPassword.attr('type', $(this).is(':checked') ? 'text' : 'password');
        $cPassword.trigger('input');
    });

    // Validate password
    function validatePassword(password) {
        return passwordRegex.test(password);
    }

    // Update form state
    function updateFormState() {
        const password = $password.val().trim();
        const cPassword = $cPassword.val().trim();
        const isPasswordValid = validatePassword(password);
        const isCPasswordValid = cPassword === password && cPassword !== '';

        if (isPasswordValid && isCPasswordValid) {
            $submitContainer.removeClass('text-start').addClass('text-end');
            $submitButton.text('Submit').prop('disabled', false);
        } else {
            $submitContainer.removeClass('text-end').addClass('text-start');
            $submitButton.text('Submit Disabled').prop('disabled', true);
        }
    }

    // Real-time validation for password
    $password.on('input', function () {
        const password = $(this).val().trim();
        const cPassword = $cPassword.val().trim();

        if (!validatePassword(password)) {
            $(this).addClass('invalid');
            $passwordError.text('Password must contain at least 8 characters, one uppercase letter, one lowercase letter, and one special character.');
        } else {
            $(this).removeClass('invalid');
            $passwordError.text('');
        }

        if (cPassword && cPassword !== password) {
            $cPassword.addClass('invalid');
            $cPasswordError.text('Passwords do not match.');
        } else {
            $cPassword.removeClass('invalid');
            $cPasswordError.text('');
        }

        updateFormState();
    });

    // Real-time validation for confirm password
    $cPassword.on('input', function () {
        const cPassword = $(this).val().trim();
        const password = $password.val().trim();

        if (cPassword !== password) {
            $(this).addClass('invalid');
            $cPasswordError.text('Passwords do not match.');
        } else {
            $(this).removeClass('invalid');
            $cPasswordError.text('');
        }

        updateFormState();
    });

    // Optional: Clear all errors (if needed elsewhere)
    function clearErrors() {
        $('.error-message').text('');
    }
});
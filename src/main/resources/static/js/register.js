$(document).ready(function() {
    // DOM elements
    const $fName = $('#f_name');
    const $lName = $('#l_name');
    const $email = $('#email');
    const $phoneNo = $('#phone_no');
    const $password = $('#password');
    const $cPassword = $('#c_password');
    const $fNameError = $('#f_name_error');
    const $lNameError = $('#l_name_error');
    const $emailError = $('#email_error');
    const $phoneNoError = $('#phone_no_error');
    const $passwordError = $('#password_error');
    const $cPasswordError = $('#c_password_error');
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

    // Form validation
    function validateForm() {
        const fName = $fName.val();
        const lName = $lName.val();
        const email = $email.val();
        const phoneNo = $phoneNo.val();
        const password = $password.val();
        const cPassword = $cPassword.val();

        const isFNameValid = validateName(fName);
        const isLNameValid = validateName(lName);
        const isEmailValid = validateEmail(email);
        const isPhoneNoValid = validatePhone(phoneNo);
        const isPasswordValid = validatePassword(password);
        const isCPasswordValid = password === cPassword && isPasswordValid;

        const isValid = isFNameValid && isLNameValid && isEmailValid && isPhoneNoValid && isPasswordValid && isCPasswordValid;

        $submitButton
            .text(isValid ? 'Submit' : 'Submit Disabled')
            .prop('disabled', !isValid);
    }

    // Real-time validation
    $fName.on('input', function() {
        const value = this.value;
        const isValid = validateName(value);
        $(this).toggleClass('invalid', !isValid);
        $fNameError.text(isValid ? '' : 'First Name cannot contain numbers or be empty');
        validateForm();
    });

    $lName.on('input', function() {
        const value = this.value;
        const isValid = validateName(value);
        $(this).toggleClass('invalid', !isValid);
        $lNameError.text(isValid ? '' : 'Last Name cannot contain numbers or be empty');
        validateForm();
    });

    $email.on('input', function() {
        const value = this.value;
        const isValid = validateEmail(value);
        $(this).toggleClass('invalid', !isValid);
        $emailError.text(isValid ? '' : 'Please enter a valid email address (e.g., example@domain.com)');
        validateForm();
    });

    $phoneNo.on('input', function() {
        const value = this.value;
        const isValid = validatePhone(value);
        $(this).toggleClass('invalid', !isValid);
        $phoneNoError.text(isValid ? '' : 'Phone number must be in format XXX-XXXXXXX (e.g., 123-4567890)');
        validateForm();
    });

    $password.on('input', function() {
        const value = this.value;
        const isValid = validatePassword(value);
        $(this).toggleClass('invalid', !isValid);
        $passwordError.text(
            isValid ? '' : 'Password must contain at least 8 characters, one uppercase, one lowercase, and one special character'
        );
        validateForm();
    });

    $cPassword.on('input', function() {
        const value = this.value;
        const password = $password.val();
        const isValid = value === password && validatePassword(value);
        $(this).toggleClass('invalid', !isValid);
        $cPasswordError.text(isValid ? '' : 'Passwords must match and meet requirements');
        validateForm();
    });

    // Validation helpers
    function validateName(name) {
        return name.trim() !== '' && !/[0-9]/.test(name);
    }

    function validateEmail(email) {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(String(email).toLowerCase());
    }

    function validatePhone(phone) {
        const re = /^[0-9]{3}-[0-9]{7,11}$/;
        return re.test(phone);
    }

    function validatePassword(password) {
        const re = /^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*?,]).{8,}$/;
        return re.test(password);
    }

    // Initialize
    validateForm();
});
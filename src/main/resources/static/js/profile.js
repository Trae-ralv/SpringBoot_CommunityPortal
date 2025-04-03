$(document).ready(function() {
    // Constants
    const MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB in bytes
    const FADE_OUT_DELAY = 5000; // 5 seconds

    // DOM Elements
    const $editLink = $('#edit-profile-link');
    const $cancelLink = $('#cancel-link');
    const $submitButton = $('#submit-button');
    const $viewModeButtons = $('#view-mode-buttons');
    const $editModeButtons = $('#edit-mode-buttons');
    const $inputs = $('.form-control:not(#profileImage)');
    const $profileImage = $('#profileImage');
    const $uploadContainer = $('.text-center');
    const $overlay = $('#resizeOverlay');
    const $previewImage = $('#previewImage');
    const $zoomSlider = $('#zoomSlider');
    const $confirmResize = $('#confirmResize');
    const $cancelResize = $('#cancelResize');
    const $resetCrop = $('#resetCrop');
    const $resizeError = $('#resizeError');
    const $successAlert = $('.alert-success');
    const $errorAlert = $('.alert-danger');

    let cropper = null;
    let selectedFile = null;

    // Form Validation Functions
    function validateForm() {
        const f_name = $('#f_name').val();
        const l_name = $('#l_name').val();
        const email = $('#email').val();
        const phone_no = $('#phone_no').val();

        const isValid = isValidName(f_name) && isValidName(l_name) &&
                       validateEmail(email) && (validatePhone(phone_no) || !phone_no.trim());

        $submitButton.text(isValid ? 'Save Changes' : 'Save Disabled').prop('disabled', !isValid);
        return isValid;
    }

    function isValidName(name) {
        return !/[0-9]/.test(name) && name.trim() !== '';
    }

    function validateEmail(email) {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(String(email).toLowerCase());
    }

    function validatePhone(phone) {
        const re = /^[0-9]{3}-[0-9]{7,11}$/;
        return re.test(phone);
    }

    // Input Validation Handlers
    function setupInputValidation(inputId, errorId, validationFn) {
        $(`#${inputId}`).on('input', function() {
            const $input = $(this);
            const value = $input.val();
            const $error = $(`#${errorId}`);

            const result = validationFn(value);
            if (typeof result === 'string') {
                $input.addClass('invalid');
                $error.text(result);
            } else {
                $input.removeClass('invalid');
                $error.text('');
            }
            validateForm();
        });
    }

    // Toggle Edit Mode
    function toggleEditMode(enable) {
        $inputs.prop('disabled', !enable);
        $viewModeButtons.toggle(!enable);
        $editModeButtons.toggle(enable);
        $uploadContainer.toggle(enable);

        if (enable) {
            clearErrors();
            validateForm();
        } else {
            clearErrors();
            $('form')[0].reset();
            $profileImage.val('');
        }
    }

    // Clear Errors
    function clearErrors() {
        $('.error-message').text('');
        $('.invalid').removeClass('invalid');
    }

    // Fade Out Alerts
    function fadeOutAlert($alert) {
        if ($alert.length) {
            setTimeout(() => {
                $alert.addClass('fade-out');
                setTimeout(() => $alert.hide(), 1000);
            }, FADE_OUT_DELAY);
        }
    }

    // Show Alert
    function showAlert(type, message) {
        const $existingAlert = $(`.alert-${type}`);
        $existingAlert.remove();

        const $alert = $('<div>')
            .addClass(`alert alert-${type} text-center`)
            .text(message);
        $('.body-container').prepend($alert);
        fadeOutAlert($alert);
    }

    // Image Upload Handling
    function handleImageUpload(file) {
        if (!file) return;

        if (file.size > MAX_FILE_SIZE) {
            $resizeError.text('File size exceeds 5MB limit. Please select a smaller image.').show();
            $profileImage.val('');
            return;
        }

        if (!file.type.startsWith('image/')) {
            $resizeError.text('Please select an image file.').show();
            $profileImage.val('');
            return;
        }

        selectedFile = file;
        const reader = new FileReader();
        reader.onload = function(e) {
            $previewImage.attr('src', e.target.result);
            $overlay.css('display', 'flex');
            $resizeError.hide();

            // Initialize Cropper.js
            if (cropper) {
                cropper.destroy();
            }
            cropper = new Cropper($previewImage[0], {
                aspectRatio: 1, // Square aspect ratio
                viewMode: 1,
                dragMode: 'move',
                cropBoxResizable: false,
                cropBoxMovable: false,
                toggleDragModeOnDblclick: false,
                minCropBoxWidth: 150,
                minCropBoxHeight: 150,
                background: false,
                center: true,
                rounded: true // Circular crop area
            });

            // Reset zoom slider
            $zoomSlider.val(1);
        };
        reader.readAsDataURL(file);
    }

    // Upload Image
    function uploadImage() {
        if (!cropper) return;

        cropper.getCroppedCanvas({
            width: 150,
            height: 150
        }).toBlob(function(blob) {
            const formData = new FormData();
            formData.append('profileImage', blob, selectedFile.name);

            $.ajax({
                url: '/upload/profileimage',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function(response) {
                    showAlert('success', 'Image uploaded successfully');
                    $('.card-img-top').attr('src', response.imageUrl);
                },
                error: function(xhr) {
                    showAlert('danger', 'Failed to upload image: ' + (xhr.responseText || 'Unknown error'));
                }
            });
        });
    }

    // Event Listeners
    $editLink.on('click', function(e) {
        e.preventDefault();
        toggleEditMode(true);
    });

    $cancelLink.on('click', function(e) {
        e.preventDefault();
        toggleEditMode(false);
    });

    setupInputValidation('f_name', 'f_name_error', value => {
        if (/[0-9]/.test(value)) return 'First Name cannot contain numbers';
        if (!value.trim()) return 'First Name is required';
        return true;
    });

    setupInputValidation('l_name', 'l_name_error', value => {
        if (/[0-9]/.test(value)) return 'Last Name cannot contain numbers';
        if (!value.trim()) return 'Last Name is required';
        return true;
    });

    setupInputValidation('email', 'email_error', value => {
        return validateEmail(value) ? true : 'Please enter a valid email address';
    });

    setupInputValidation('phone_no', 'phone_no_error', value => {
        return !value.trim() || validatePhone(value) ? true : 'Please enter a valid phone number (e.g., 123-4567890)';
    });

    $profileImage.on('change', function() {
        handleImageUpload(this.files[0]);
    });

    $zoomSlider.on('input', function() {
        if (cropper) {
            cropper.zoomTo(parseFloat(this.value));
        }
    });

    $confirmResize.on('click', function() {
        uploadImage();
        $overlay.hide();
        $profileImage.val('');
        if (cropper) {
            cropper.destroy();
            cropper = null;
        }
    });

    $cancelResize.on('click', function() {
        $overlay.hide();
        $profileImage.val('');
        $resizeError.hide();
        if (cropper) {
            cropper.destroy();
            cropper = null;
        }
    });

    $resetCrop.on('click', function() {
        if (cropper) {
            cropper.reset();
            $zoomSlider.val(1);
        }
    });

    // Initial Setup
    $uploadContainer.hide();
    fadeOutAlert($successAlert);
    fadeOutAlert($errorAlert);
});
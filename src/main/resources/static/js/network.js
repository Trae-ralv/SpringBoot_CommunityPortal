$(document).ready(function() {
    function debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }

    const searchUsers = debounce(function(searchTerm) {
        // Trigger search for any length (including 1 character)
        $.ajax({
            url: '/network/search',
            type: 'GET',
            data: { query: searchTerm },
            success: function(users) {
                updateUserResults(users);
            },
            error: function(xhr, status, error) {
                console.error('Search error:', error);
            }
        });
        // Only reload when search bar is completely empty
        if (searchTerm.length === 0) {
            location.reload();
        }
    }, 300);

    $('#searchInput').on('input', function() {
        const searchTerm = $(this).val().trim();
        searchUsers(searchTerm);
    });

    function updateUserResults(users) {
        const $resultsContainer = $('#userResults');
        $resultsContainer.empty();

        if (users.length === 0) {
            $resultsContainer.append(
                '<div class="col-12 text-center">No users found</div>'
            );
            return;
        }

        users.forEach(user => {
            const userCard = `
                <div class="col">
                    <div class="card user-card h-100 shadow-sm">
                        <img src="/images/default-avatar.svg"
                             class="card-img-top mt-3"
                             alt="User Image"
                             height="100px">
                        <div class="card-body text-center">
                            <h5 class="card-title">${user.f_name} ${user.l_name}</h5>
                            <a href="/networkprofile/${user.user_id}"
                               class="btn btn-primary btn-sm mt-2">View Profile</a>
                        </div>
                    </div>
                </div>
            `;
            $resultsContainer.append(userCard);
        });
    }

    $('.user-card').hover(
        function() {
            $(this).addClass('shadow-lg').css('cursor', 'pointer');
        },
        function() {
            $(this).removeClass('shadow-lg');
        }
    );
});
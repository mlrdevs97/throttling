/**
 * Handles algorithm selection and navigation
 * @param {string} algorithm - The algorithm to select ('token-bucket' or 'leaky-bucket')
 */
function selectAlgorithm(algorithm) {
    // Add visual feedback to the selected card
    const card = document.querySelector(`[data-algorithm="${algorithm}"]`);
    if (card) {
        card.classList.add('selected');
        
        // Remove the selected class after animation
        setTimeout(() => {
            card.classList.remove('selected');
        }, 300);
    }

    // Navigate to the appropriate visualizer page
    setTimeout(() => {
        window.location.href = `./visualizer.html?algorithm=${algorithm}`;
    }, 150);
}

// Add keyboard navigation support
document.addEventListener('keydown', (event) => {
    if (event.key === '1') {
        selectAlgorithm('token-bucket');
    } else if (event.key === '2') {
        selectAlgorithm('leaky-bucket');
    }
});

// Add hover effects for better UX
document.addEventListener('DOMContentLoaded', () => {
    const algorithmCards = document.querySelectorAll('.algorithm-card');
    
    algorithmCards.forEach(card => {
        card.addEventListener('mouseenter', () => {
            card.style.transform = 'translateY(-5px)';
        });
        
        card.addEventListener('mouseleave', () => {
            card.style.transform = 'translateY(0)';
        });
    });
});

// Add click handlers to cards (alternative to button click)
document.addEventListener('DOMContentLoaded', () => {
    const algorithmCards = document.querySelectorAll('.algorithm-card');
    
    algorithmCards.forEach(card => {
        card.addEventListener('click', (event) => {
            // Don't trigger if clicking on the button
            if (event.target.tagName === 'BUTTON') {
                return;
            }
            
            const algorithm = card.dataset.algorithm;
            selectAlgorithm(algorithm);
        });
    });
}); 
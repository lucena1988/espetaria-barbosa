document.addEventListener('DOMContentLoaded', () => {
    const toggles = document.querySelectorAll('[data-theme-toggle]');

    function applyTheme(useLight) {
        document.documentElement.classList.toggle('light-theme', useLight);
        localStorage.setItem('espetariaTema', useLight ? 'light' : 'dark');

        toggles.forEach((toggle) => {
            const label = toggle.querySelector('[data-theme-label]');
            const icon = toggle.querySelector('[data-theme-icon]');
            toggle.setAttribute('aria-pressed', String(useLight));
            toggle.title = useLight ? 'Usar modo dark' : 'Usar modo light';

            if (label) {
                label.textContent = useLight ? 'Modo dark' : 'Modo light';
            }

            if (icon) {
                icon.textContent = useLight ? 'L' : 'D';
            }
        });
    }

    toggles.forEach((toggle) => {
        toggle.addEventListener('click', () => {
            applyTheme(!document.documentElement.classList.contains('light-theme'));
        });
    });

    applyTheme(localStorage.getItem('espetariaTema') === 'light');
});

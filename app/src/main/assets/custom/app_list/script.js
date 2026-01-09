// Глобальная переменная для хранения всех приложений
let allApps = [];
let currentFilter = 'all';
let currentSearch = '';

// JavaScript интерфейс для связи с Android
class AndroidInterface {
    // Метод для получения списка приложений
    static getApps() {
        if (window.Android && window.Android.getApps) {
            return JSON.parse(window.Android.getApps());
        }
        return [];
    }

    // Метод для запуска приложения
    static launchApp(packageName) {
        if (window.Android && window.Android.launchApp) {
            window.Android.launchApp(packageName);
        }
    }

    // Метод для возврата назад
    static goBack() {
        if (window.Android && window.Android.goBack) {
            window.Android.goBack();
        }
    }

    // Метод для получения информации о системе
    static getSystemInfo() {
        if (window.Android && window.Android.getSystemInfo) {
            return JSON.parse(window.Android.getSystemInfo());
        }
        return { totalApps: 0, enabledApps: 0, disabledApps: 0 };
    }
}

// Функция для возврата назад
function goBack() {
    AndroidInterface.goBack();
}

// Функция для фильтрации приложений
function filterApps() {
    const searchInput = document.getElementById('searchInput');
    currentSearch = searchInput.value.toLowerCase().trim();
    renderApps();
}

// Функция для установки фильтра
function setFilter(filter) {
    currentFilter = filter;

    // Обновляем активную кнопку фильтра
    document.querySelectorAll('.filter-button').forEach(btn => {
        if (btn.getAttribute('data-filter') === filter) {
            btn.classList.add('active');
        } else {
            btn.classList.remove('active');
        }
    });

    renderApps();
}

// Функция для рендеринга приложений
function renderApps() {
    const appsGrid = document.getElementById('appsGrid');
    const loadingElement = document.getElementById('loading');
    const emptyStateElement = document.getElementById('emptyState');

    // Показываем загрузку
    loadingElement.style.display = 'flex';
    emptyStateElement.style.display = 'none';
    appsGrid.innerHTML = '';

    // Фильтруем приложения
    const filteredApps = allApps.filter(app => {
        // Поиск по названию
        const matchesSearch = currentSearch === '' ||
            app.appName.toLowerCase().includes(currentSearch) ||
            app.packageName.toLowerCase().includes(currentSearch);

        // Фильтр по типу/состоянию
        let matchesFilter = true;
        switch (currentFilter) {
            case 'enabled':
                matchesFilter = app.enabled === true;
                break;
            case 'disabled':
                matchesFilter = app.enabled === false;
                break;
            case 'system':
                matchesFilter = app.isSystemApp === true;
                break;
            case 'user':
                matchesFilter = app.isSystemApp === false;
                break;
            case 'all':
            default:
                matchesFilter = true;
        }

        return matchesSearch && matchesFilter;
    });

    // Имитируем задержку для плавности
    setTimeout(() => {
        loadingElement.style.display = 'none';

        if (filteredApps.length === 0) {
            emptyStateElement.style.display = 'block';
            return;
        }

        // Сортируем приложения по алфавиту
        filteredApps.sort((a, b) => a.appName.localeCompare(b.appName));

        // Рендерим приложения
        filteredApps.forEach(app => {
            const appElement = document.createElement('div');
            appElement.className = 'app-item';
            appElement.onclick = () => {
                if (app.enabled) {
                    AndroidInterface.launchApp(app.packageName);
                } else {
                    alert('Приложение отключено и не может быть запущено');
                }
            };

            // Создаем элемент иконки
            const iconElement = document.createElement('img');
            iconElement.className = 'app-icon';
            iconElement.src = app.iconBase64 || 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDgiIGhlaWdodD0iNDgiIHZpZXdCb3g9IjAgMCA0OCA0OCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iNDgiIGhlaWdodD0iNDgiIHJ4PSIxMiIgZmlsbD0iIzIxOTZGMiIvPjxwYXRoIGQ9Ik0yNCAzMkMzMy4wNTY3IDMyIDQwIDI1LjA1NjcgNDAgMTZDNDAgNi45NDMzIDMzLjA1NjcgMCAyNCAwQzE0Ljk0MzMgMCA4IDYuOTQzMyA4IDE2QzggMjUuMDU2NyAxNC45NDMzIDMyIDI0IDMyWiIgZmlsbD0id2hpdGUiLz48cGF0aCBkPSJNMTcuNjg3NSAyMS43NUwzMC4zMTI1IDIxLjc1QzMwLjcxODggMjEuNzUgMzEuMDMxMyAyMi4wNjI1IDMxLjAzMTMgMjIuNDY4OFYyNS41MzEyQzMxLjAzMTMgMjUuOTM3NSAzMC43MTg4IDI2LjI1IDMwLjMxMjUgMjYuMjVIMTcuNjg3NUMxNy4yODEzIDI2LjI1IDE2Ljk2ODggMjUuOTM3NSAxNi45Njg4IDI1LjUzMTJWMjIuNDY4OEMxNi45Njg4IDIyLjA2MjUgMTcuMjgxMyAyMS43NSAxNy42ODc1IDIxLjc1WiIgZmlsbD0id2hpdGUiLz48L3N2Zz4=';

            // Если приложение отключено, делаем иконку серой
            if (!app.enabled) {
                iconElement.style.filter = 'grayscale(100%) opacity(0.6)';
            }

            // Создаем элемент названия
            const nameElement = document.createElement('div');
            nameElement.className = 'app-name';
            nameElement.textContent = app.appName;

            // Создаем элемент package (скрытый)
            const packageElement = document.createElement('div');
            packageElement.className = 'app-package';
            packageElement.textContent = app.packageName;

            appElement.appendChild(iconElement);
            appElement.appendChild(nameElement);
            appElement.appendChild(packageElement);

            // Добавляем бейдж для системных приложений
            if (app.isSystemApp) {
                const badgeElement = document.createElement('div');
                badgeElement.className = 'app-badge';
                badgeElement.textContent = 'SYS';
                appElement.appendChild(badgeElement);
                appElement.style.position = 'relative';
            }

            appsGrid.appendChild(appElement);
        });

        // Обновляем статистику в заголовке
        updateStats(filteredApps.length);
    }, 100);
}

// Функция для обновления статистики
function updateStats(count) {
    const titleElement = document.getElementById('pageTitle');
    const totalApps = allApps.length;
    titleElement.textContent = `Приложения (${count}/${totalApps})`;
}

// Функция для загрузки приложений
function loadApps() {
    // Получаем приложения из Android
    allApps = AndroidInterface.getApps();

    // Получаем системную информацию
    const systemInfo = AndroidInterface.getSystemInfo();

    // Рендерим приложения
    renderApps();

    // Обновляем фильтры с количеством
    updateFilterCounts();
}

// Функция для обновления счетчиков в фильтрах
function updateFilterCounts() {
    const enabledCount = allApps.filter(app => app.enabled).length;
    const disabledCount = allApps.filter(app => !app.enabled).length;
    const systemCount = allApps.filter(app => app.isSystemApp).length;
    const userCount = allApps.filter(app => !app.isSystemApp).length;

    const filterButtons = document.querySelectorAll('.filter-button');
    filterButtons.forEach(btn => {
        const filterType = btn.getAttribute('data-filter');
        const baseText = btn.textContent.replace(/\(\d+\)/g, '').trim();

        switch (filterType) {
            case 'enabled':
                btn.textContent = `Включенные (${enabledCount})`;
                break;
            case 'disabled':
                btn.textContent = `Отключенные (${disabledCount})`;
                break;
            case 'system':
                btn.textContent = `Системные (${systemCount})`;
                break;
            case 'user':
                btn.textContent = `Пользовательские (${userCount})`;
                break;
            case 'all':
                btn.textContent = `Все (${allApps.length})`;
                break;
        }
    });
}

// Функция для обработки жестов свайпа
function initSwipeGestures() {
    let touchStartY = 0;
    let touchStartX = 0;

    document.addEventListener('touchstart', (e) => {
        touchStartY = e.touches[0].clientY;
        touchStartX = e.touches[0].clientX;
    }, { passive: true });

    document.addEventListener('touchend', (e) => {
        const touchEndY = e.changedTouches[0].clientY;
        const touchEndX = e.changedTouches[0].clientX;
        const deltaY = touchEndY - touchStartY;
        const deltaX = touchEndX - touchStartX;

        // Если свайп вверх достаточно большой, скроллим вверх
        if (deltaY < -50 && Math.abs(deltaX) < Math.abs(deltaY)) {
            window.scrollTo({ top: 0, behavior: 'smooth' });
        }
    }, { passive: true });
}

// Функция для инициализации обработчиков событий
function initEventListeners() {
    // Кнопка "Назад"
    document.getElementById('backButton').addEventListener('click', goBack);

    // Поле поиска
    document.getElementById('searchInput').addEventListener('input', filterApps);

    // Кнопки фильтров
    document.querySelectorAll('.filter-button').forEach(button => {
        button.addEventListener('click', (e) => {
            setFilter(e.target.getAttribute('data-filter'));
        });
    });
}

// Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', () => {
    initEventListeners();
    initSwipeGestures();
    loadApps();

    // Фокус на поле поиска при загрузке
    setTimeout(() => {
        const searchInput = document.getElementById('searchInput');
        if (searchInput) {
            searchInput.focus();
        }
    }, 300);
});

// Функция для обновления списка приложений извне
window.updateAppList = function(apps) {
    allApps = apps;
    renderApps();
};

// Функция для обновления иконки приложения
window.updateAppIcon = function(packageName, base64Icon) {
    const appElement = document.querySelector(`[data-package="${packageName}"]`);
    if (appElement) {
        const iconElement = appElement.querySelector('.app-icon');
        if (iconElement) {
            iconElement.src = base64Icon;
        }
    }
};
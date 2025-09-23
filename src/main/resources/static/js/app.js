// 校园失物招领系统 - 主JavaScript文件

document.addEventListener('DOMContentLoaded', function() {
    // 初始化应用
    initializeApp();
});

/**
 * 初始化应用
 */
function initializeApp() {
    // 初始化返回顶部按钮
    initBackToTopButton();
    
    // 初始化加载动画
    initLoadingSpinner();
    
    // 初始化工具提示
    initTooltips();
    
    // 初始化表单验证
    initFormValidation();
    
    // 初始化动画效果
    initAnimations();
    
    // 初始化搜索功能
    initSearchFunctionality();
    
    // 初始化图片预览
    initImagePreview();
    
    // 初始化通知系统
    initNotificationSystem();
    
    console.log('校园失物招领系统初始化完成');
}

/**
 * 初始化返回顶部按钮
 */
function initBackToTopButton() {
    const backToTopBtn = document.getElementById('backToTop');
    if (!backToTopBtn) return;
    
    // 监听滚动事件
    window.addEventListener('scroll', function() {
        if (window.pageYOffset > 300) {
            backToTopBtn.style.display = 'block';
            backToTopBtn.classList.add('animate__fadeInUp');
        } else {
            backToTopBtn.style.display = 'none';
            backToTopBtn.classList.remove('animate__fadeInUp');
        }
    });
    
    // 点击返回顶部
    backToTopBtn.addEventListener('click', function() {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    });
}

/**
 * 初始化加载动画
 */
function initLoadingSpinner() {
    const loadingSpinner = document.getElementById('loadingSpinner');
    
    // 显示加载动画
    window.showLoading = function() {
        if (loadingSpinner) {
            loadingSpinner.style.display = 'flex';
        }
    };
    
    // 隐藏加载动画
    window.hideLoading = function() {
        if (loadingSpinner) {
            loadingSpinner.style.display = 'none';
        }
    };
    
    // 监听所有表单提交
    document.addEventListener('submit', function(e) {
        if (e.target.tagName === 'FORM') {
            showLoading();
        }
    });
    
    // 监听所有链接点击
    document.addEventListener('click', function(e) {
        if (e.target.tagName === 'A' && e.target.href && !e.target.href.includes('#')) {
            showLoading();
        }
    });
}

/**
 * 初始化工具提示
 */
function initTooltips() {
    // 使用Bootstrap的工具提示
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function(tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
}

/**
 * 初始化表单验证
 */
function initFormValidation() {
    const forms = document.querySelectorAll('.needs-validation');
    
    Array.from(forms).forEach(form => {
        form.addEventListener('submit', event => {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            
            form.classList.add('was-validated');
        }, false);
    });
}

/**
 * 初始化动画效果
 */
function initAnimations() {
    // 为卡片添加进入动画
    const cards = document.querySelectorAll('.card');
    cards.forEach((card, index) => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(30px)';
        
        setTimeout(() => {
            card.style.transition = 'all 0.6s ease';
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, index * 100);
    });
    
    // 为列表项添加进入动画
    const listItems = document.querySelectorAll('.list-group-item');
    listItems.forEach((item, index) => {
        item.style.opacity = '0';
        item.style.transform = 'translateX(-30px)';
        
        setTimeout(() => {
            item.style.transition = 'all 0.5s ease';
            item.style.opacity = '1';
            item.style.transform = 'translateX(0)';
        }, index * 50);
    });
}

/**
 * 初始化搜索功能
 */
function initSearchFunctionality() {
    const searchInput = document.getElementById('searchInput');
    if (!searchInput) return;
    
    let searchTimeout;
    
    searchInput.addEventListener('input', function() {
        clearTimeout(searchTimeout);
        const query = this.value.trim();
        
        if (query.length >= 2) {
            searchTimeout = setTimeout(() => {
                performSearch(query);
            }, 300);
        }
    });
    
    // 搜索建议
    function performSearch(query) {
        // 这里可以添加实时搜索功能
        console.log('搜索:', query);
    }
}

/**
 * 初始化图片预览
 */
function initImagePreview() {
    const imageLinks = document.querySelectorAll('a[data-image-preview]');
    
    imageLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const imageSrc = this.href;
            showImagePreview(imageSrc);
        });
    });
    
    function showImagePreview(src) {
        const modal = document.createElement('div');
        modal.className = 'modal fade';
        modal.innerHTML = `
            <div class="modal-dialog modal-lg modal-dialog-centered">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">图片预览</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body text-center">
                        <img src="${src}" class="img-fluid rounded" alt="预览图片">
                    </div>
                </div>
            </div>
        `;
        
        document.body.appendChild(modal);
        const bsModal = new bootstrap.Modal(modal);
        bsModal.show();
        
        modal.addEventListener('hidden.bs.modal', function() {
            document.body.removeChild(modal);
        });
    }
}

/**
 * 初始化通知系统
 */
function initNotificationSystem() {
    // 显示通知消息
    window.showNotification = function(message, type = 'info') {
        const alertDiv = document.createElement('div');
        alertDiv.className = `alert alert-${type} alert-dismissible fade show position-fixed`;
        alertDiv.style.cssText = 'top: 100px; right: 20px; z-index: 9999; min-width: 300px;';
        alertDiv.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;
        
        document.body.appendChild(alertDiv);
        
        // 自动消失
        setTimeout(() => {
            if (alertDiv.parentNode) {
                alertDiv.remove();
            }
        }, 5000);
    };
    
    // 显示成功消息
    window.showSuccess = function(message) {
        showNotification(message, 'success');
    };
    
    // 显示错误消息
    window.showError = function(message) {
        showNotification(message, 'danger');
    };
    
    // 显示警告消息
    window.showWarning = function(message) {
        showNotification(message, 'warning');
    };
}

/**
 * 格式化日期
 */
function formatDate(dateString) {
    const date = new Date(dateString);
    const now = new Date();
    const diffTime = Math.abs(now - date);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    
    if (diffDays === 1) {
        return '昨天';
    } else if (diffDays < 7) {
        return `${diffDays}天前`;
    } else {
        return date.toLocaleDateString('zh-CN');
    }
}

/**
 * 复制到剪贴板
 */
function copyToClipboard(text) {
    navigator.clipboard.writeText(text).then(() => {
        showSuccess('已复制到剪贴板');
    }).catch(() => {
        showError('复制失败');
    });
}

/**
 * 确认对话框
 */
function confirmAction(message, callback) {
    if (confirm(message)) {
        callback();
    }
}

/**
 * 防抖函数
 */
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

/**
 * 节流函数
 */
function throttle(func, limit) {
    let inThrottle;
    return function() {
        const args = arguments;
        const context = this;
        if (!inThrottle) {
            func.apply(context, args);
            inThrottle = true;
            setTimeout(() => inThrottle = false, limit);
        }
    };
}

/**
 * 本地存储工具
 */
const Storage = {
    set: function(key, value) {
        try {
            localStorage.setItem(key, JSON.stringify(value));
        } catch (e) {
            console.error('存储失败:', e);
        }
    },
    
    get: function(key) {
        try {
            const item = localStorage.getItem(key);
            return item ? JSON.parse(item) : null;
        } catch (e) {
            console.error('读取失败:', e);
            return null;
        }
    },
    
    remove: function(key) {
        try {
            localStorage.removeItem(key);
        } catch (e) {
            console.error('删除失败:', e);
        }
    },
    
    clear: function() {
        try {
            localStorage.clear();
        } catch (e) {
            console.error('清空失败:', e);
        }
    }
};

/**
 * 网络请求工具
 */
const Api = {
    request: function(url, options = {}) {
        const defaultOptions = {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        };
        
        const finalOptions = { ...defaultOptions, ...options };
        
        return fetch(url, finalOptions)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .catch(error => {
                console.error('请求失败:', error);
                showError('网络请求失败');
                throw error;
            });
    },
    
    get: function(url) {
        return this.request(url);
    },
    
    post: function(url, data) {
        return this.request(url, {
            method: 'POST',
            body: JSON.stringify(data),
        });
    },
    
    put: function(url, data) {
        return this.request(url, {
            method: 'PUT',
            body: JSON.stringify(data),
        });
    },
    
    delete: function(url) {
        return this.request(url, {
            method: 'DELETE',
        });
    }
};

// 导出到全局作用域
window.Storage = Storage;
window.Api = Api;
window.formatDate = formatDate;
window.copyToClipboard = copyToClipboard;
window.confirmAction = confirmAction;
window.debounce = debounce;
window.throttle = throttle;

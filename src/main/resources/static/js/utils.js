// Utility functions for LangGraph Web UI

const LangGraphUtils = {
    
    formatTimestamp(timestamp) {
        const date = new Date(timestamp);
        return date.toLocaleString('zh-CN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
            hour12: false
        });
    },
    
    formatDuration(milliseconds) {
        const seconds = Math.floor(milliseconds / 1000);
        const minutes = Math.floor(seconds / 60);
        const hours = Math.floor(minutes / 60);
        
        if (hours > 0) {
            return `${hours}h ${minutes % 60}m ${seconds % 60}s`;
        } else if (minutes > 0) {
            return `${minutes}m ${seconds % 60}s`;
        } else {
            return `${seconds}s`;
        }
    },
    
    getEventIcon(eventType) {
        const iconMap = {
            'EXECUTION_STARTED': 'bi-play-circle',
            'EXECUTION_COMPLETED': 'bi-check-circle',
            'EXECUTION_FAILED': 'bi-x-circle',
            'NODE_STARTED': 'bi-arrow-right-circle',
            'NODE_COMPLETED': 'bi-check2-circle',
            'NODE_FAILED': 'bi-exclamation-circle'
        };
        return iconMap[eventType] || 'bi-circle';
    },
    
    getEventColor(eventType) {
        if (eventType.includes('STARTED')) return 'info';
        if (eventType.includes('COMPLETED')) return 'success';
        if (eventType.includes('FAILED')) return 'danger';
        return 'secondary';
    },
    
    formatJSON(obj) {
        return JSON.stringify(obj, null, 2);
    },
    
    copyToClipboard(text) {
        navigator.clipboard.writeText(text).then(() => {
            this.showToast('已复制到剪贴板', 'success');
        }).catch(err => {
            console.error('Failed to copy:', err);
            this.showToast('复制失败', 'danger');
        });
    },
    
    showToast(message, type = 'info') {
        const toastContainer = document.getElementById('toastContainer');
        if (!toastContainer) {
            const container = document.createElement('div');
            container.id = 'toastContainer';
            container.className = 'toast-container position-fixed top-0 end-0 p-3';
            document.body.appendChild(container);
        }
        
        const toast = document.createElement('div');
        toast.className = `toast align-items-center text-white bg-${type} border-0`;
        toast.setAttribute('role', 'alert');
        toast.innerHTML = `
            <div class="d-flex">
                <div class="toast-body">${message}</div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        `;
        
        document.getElementById('toastContainer').appendChild(toast);
        const bsToast = new bootstrap.Toast(toast);
        bsToast.show();
        
        toast.addEventListener('hidden.bs.toast', () => toast.remove());
    },
    
    downloadJSON(data, filename) {
        const blob = new Blob([this.formatJSON(data)], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        a.click();
        URL.revokeObjectURL(url);
    },
    
    truncateId(id, length = 8) {
        return id.substring(0, length) + '...';
    },
    
    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
};

// Export for use in modules
if (typeof module !== 'undefined' && module.exports) {
    module.exports = LangGraphUtils;
}

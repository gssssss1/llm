// Dynamic Execution Graph Renderer

class DynamicExecutionGraph {
    constructor(containerId) {
        this.container = document.getElementById(containerId);
        this.nodes = new Map();
        this.edges = new Map();
        this.executedNodes = new Set();
        this.currentNode = null;
        this.graphData = null;
        
        this.initContainer();
    }
    
    initContainer() {
        this.container.innerHTML = `
            <div class="dynamic-graph-wrapper">
                <div class="dynamic-graph-canvas" id="dynamicGraphCanvas">
                    <div class="graph-empty-state">
                        <i class="bi bi-play-circle" style="font-size: 3rem; color: #dee2e6;"></i>
                        <p class="text-muted mt-3">等待执行开始...</p>
                    </div>
                </div>
                <div class="graph-legend">
                    <div class="legend-item">
                        <span class="legend-dot" style="background: #0d6efd;"></span>
                        <span>执行中</span>
                    </div>
                    <div class="legend-item">
                        <span class="legend-dot" style="background: #198754;"></span>
                        <span>已完成</span>
                    </div>
                    <div class="legend-item">
                        <span class="legend-dot" style="background: #dc3545;"></span>
                        <span>失败</span>
                    </div>
                    <div class="legend-item">
                        <span class="legend-dot" style="background: #6c757d;"></span>
                        <span>等待中</span>
                    </div>
                </div>
            </div>
        `;
        
        this.canvas = document.getElementById('dynamicGraphCanvas');
    }
    
    setGraphData(graphData) {
        this.graphData = graphData;
    }
    
    reset() {
        this.nodes.clear();
        this.edges.clear();
        this.executedNodes.clear();
        this.currentNode = null;
        this.canvas.innerHTML = `
            <div class="graph-empty-state">
                <i class="bi bi-play-circle" style="font-size: 3rem; color: #dee2e6;"></i>
                <p class="text-muted mt-3">等待执行开始...</p>
            </div>
        `;
    }
    
    onNodeStarted(nodeName) {
        this.currentNode = nodeName;
        
        if (!this.nodes.has(nodeName)) {
            this.addNode(nodeName, 'running');
        } else {
            this.updateNodeStatus(nodeName, 'running');
        }
        
        this.addIncomingEdges(nodeName);
    }
    
    onNodeCompleted(nodeName) {
        this.executedNodes.add(nodeName);
        this.updateNodeStatus(nodeName, 'completed');
        
        if (this.graphData) {
            this.addOutgoingEdges(nodeName);
        }
    }
    
    onNodeFailed(nodeName) {
        this.updateNodeStatus(nodeName, 'failed');
    }
    
    addNode(nodeName, status = 'pending') {
        if (this.nodes.size === 0) {
            this.canvas.innerHTML = '';
        }
        
        const nodeElement = document.createElement('div');
        nodeElement.className = 'dynamic-node';
        nodeElement.id = `node-${nodeName}`;
        nodeElement.setAttribute('data-status', status);
        
        nodeElement.innerHTML = `
            <div class="node-content">
                <div class="node-icon">
                    <i class="bi ${this.getNodeIcon(status)}"></i>
                </div>
                <div class="node-label">${nodeName}</div>
                <div class="node-status-indicator"></div>
            </div>
        `;
        
        this.canvas.appendChild(nodeElement);
        this.nodes.set(nodeName, nodeElement);
        
        setTimeout(() => {
            nodeElement.classList.add('node-appear');
        }, 10);
        
        this.repositionNodes();
    }
    
    updateNodeStatus(nodeName, status) {
        const node = this.nodes.get(nodeName);
        if (node) {
            node.setAttribute('data-status', status);
            const icon = node.querySelector('.node-icon i');
            if (icon) {
                icon.className = 'bi ' + this.getNodeIcon(status);
            }
        }
    }
    
    addIncomingEdges(nodeName) {
        if (!this.graphData) return;
        
        const edges = this.graphData.edges.filter(edge => edge.to === nodeName);
        edges.forEach(edge => {
            if (this.executedNodes.has(edge.from)) {
                this.addEdge(edge.from, edge.to, edge.type);
            }
        });
    }
    
    addOutgoingEdges(nodeName) {
        if (!this.graphData) return;
        
        const edges = this.graphData.edges.filter(edge => edge.from === nodeName);
        edges.forEach(edge => {
            const edgeKey = `${edge.from}-${edge.to}`;
            if (!this.edges.has(edgeKey)) {
                setTimeout(() => {
                    this.addEdge(edge.from, edge.to, edge.type);
                }, 300);
            }
        });
    }
    
    addEdge(from, to, type = 'static') {
        const edgeKey = `${from}-${to}`;
        if (this.edges.has(edgeKey)) return;
        
        const fromNode = this.nodes.get(from);
        const toNode = this.nodes.get(to);
        
        if (!fromNode || !toNode) {
            return;
        }
        
        const edgeElement = document.createElement('div');
        edgeElement.className = 'dynamic-edge';
        edgeElement.setAttribute('data-type', type);
        
        const arrow = document.createElement('div');
        arrow.className = 'edge-arrow';
        arrow.innerHTML = '<i class="bi bi-arrow-right"></i>';
        edgeElement.appendChild(arrow);
        
        this.canvas.insertBefore(edgeElement, this.canvas.firstChild);
        this.edges.set(edgeKey, edgeElement);
        
        this.positionEdge(edgeElement, fromNode, toNode);
        
        setTimeout(() => {
            edgeElement.classList.add('edge-appear');
        }, 10);
    }
    
    positionEdge(edgeElement, fromNode, toNode) {
        const fromRect = fromNode.getBoundingClientRect();
        const toRect = toNode.getBoundingClientRect();
        const canvasRect = this.canvas.getBoundingClientRect();
        
        const fromX = fromRect.left - canvasRect.left + fromRect.width / 2;
        const fromY = fromRect.top - canvasRect.top + fromRect.height / 2;
        const toX = toRect.left - canvasRect.left + toRect.width / 2;
        const toY = toRect.top - canvasRect.top + toRect.height / 2;
        
        const angle = Math.atan2(toY - fromY, toX - fromX);
        const length = Math.sqrt(Math.pow(toX - fromX, 2) + Math.pow(toY - fromY, 2));
        
        edgeElement.style.left = fromX + 'px';
        edgeElement.style.top = fromY + 'px';
        edgeElement.style.width = length + 'px';
        edgeElement.style.transform = `rotate(${angle}rad)`;
    }
    
    repositionNodes() {
        const nodeArray = Array.from(this.nodes.values());
        const columns = Math.ceil(Math.sqrt(nodeArray.length));
        const nodeSpacing = 150;
        const startX = 50;
        const startY = 50;
        
        nodeArray.forEach((node, index) => {
            const col = index % columns;
            const row = Math.floor(index / columns);
            
            node.style.left = (startX + col * nodeSpacing) + 'px';
            node.style.top = (startY + row * nodeSpacing) + 'px';
        });
        
        this.edges.forEach((edge, key) => {
            const [from, to] = key.split('-');
            const fromNode = this.nodes.get(from);
            const toNode = this.nodes.get(to);
            if (fromNode && toNode) {
                this.positionEdge(edge, fromNode, toNode);
            }
        });
    }
    
    getNodeIcon(status) {
        const icons = {
            'pending': 'bi-circle',
            'running': 'bi-play-circle-fill',
            'completed': 'bi-check-circle-fill',
            'failed': 'bi-x-circle-fill'
        };
        return icons[status] || 'bi-circle';
    }
    
    highlightPath(fromNode, toNode) {
        const edge = this.edges.get(`${fromNode}-${toNode}`);
        if (edge) {
            edge.classList.add('edge-highlight');
            setTimeout(() => {
                edge.classList.remove('edge-highlight');
            }, 1000);
        }
    }
}

// Export for use in modules
if (typeof module !== 'undefined' && module.exports) {
    module.exports = DynamicExecutionGraph;
}

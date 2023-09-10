// Parameters
let activeProcessTab = 0;
let activeISETab = 0;
let alreadyLoadedDiagrams = new Set();
let canvasElementWithBPMNJS = new Map();

// Initializes Page
initProcessTab();
initISETab();

function initProcessTab(){
    const processTabContainer = document.getElementById("processTabContainer");

    const processNames = processTabContainer.getElementsByClassName("containerTabsButton");
    processNames[activeProcessTab].style.fontWeight = "bold";

    const processNameUnderline = processTabContainer.getElementsByClassName("containerLinesDiv");
    processNameUnderline[activeProcessTab].style.backgroundColor = "#BFAE5A";

    const processTabs = processTabContainer.getElementsByClassName("containerTabContent");
    processTabs[activeProcessTab].style.display = "flex";
}

function initISETab(){
    const iseTabContainer = document.getElementById("iseTabContainer");

    const iseNames = iseTabContainer.getElementsByClassName("containerTabsButton");
    iseNames[0].style.fontWeight = "bold";

    const iseNameUnderline = iseTabContainer.getElementsByClassName("containerLinesDiv");
    iseNameUnderline[0].style.backgroundColor = "#BFAE5A";

    const iseTabs = iseTabContainer.getElementsByClassName("containerTabContent");
    iseTabs[0].style.display = "flex";
}

function changeProcessTab(element){
    const processTabContainer = document.getElementById("processTabContainer");
    const processNames = processTabContainer.getElementsByClassName("containerTabsButton");
    const newProcessTab = Array.from(processNames).indexOf(element);
    changeTab(processTabContainer, activeProcessTab, newProcessTab);
    activeProcessTab = newProcessTab;
}

function changeISETab(element){
    const iseTabContainer = document.getElementById("iseTabContainer");
    const iseNames = iseTabContainer.getElementsByClassName("containerTabsButton");
    const newISETab = Array.from(iseNames).indexOf(element);
    changeTab(iseTabContainer, activeISETab, newISETab);
    activeISETab = newISETab;
}

function changeTab(tabContainer, originIndex, targetIndex){
    const names = tabContainer.getElementsByClassName("containerTabsButton");
    names[originIndex].style.fontWeight = "normal";
    names[targetIndex].style.fontWeight = "bold";

    const underline = tabContainer.getElementsByClassName("containerLinesDiv");
    underline[originIndex].style.backgroundColor = "#D3D7DA";
    underline[targetIndex].style.backgroundColor = "#BFAE5A";

    const tab = tabContainer.getElementsByClassName("containerTabContent");
    tab[originIndex].style.display = "none";
    tab[targetIndex].style.display = "flex";
}

function changeVisibilityISE(element){
    const visualizationContainer = element.parentNode;

    const ppiContainer = visualizationContainer.getElementsByClassName("ppiContainer")[0];
    const modelContainer = visualizationContainer.getElementsByClassName("visualizationISEModel")[0];
    const img = element.getElementsByClassName("visualizationISEImage")[0];

    if(ppiContainer.style.display == "none" || ppiContainer.style.display == ""){
        ppiContainer.style.display = "flex";
        modelContainer.style.display = "flex";
        element.style.borderBottomRightRadius = "0px";
        element.style.borderBottomLeftRadius = "0px";
        element.style.backgroundColor = "#053259";
        visualizationContainer.style.borderColor = "#042440";
        img.transform = 'rotate(90deg)'
        if(!alreadyLoadedDiagrams.has(element)){
            loadDiagram(visualizationContainer.getElementsByClassName("visualizationISEModel")[0]);
            alreadyLoadedDiagrams.add(element);
        }
    }else{
        ppiContainer.style.display = "none";
        modelContainer.style.display = "none";
        element.style.borderBottomRightRadius = "17px";
        element.style.borderBottomLeftRadius = "17px";
        element.style.backgroundColor = "rgba(5,50,89,0.5)";
        visualizationContainer.style.borderColor = "rgba(5,50,89,0.5)";
        img.transform = 'rotate(90deg)'
    }
}

async function loadDiagram(canvasElement){
    const canvasID = canvasElement.id;
    const modelID = parseInt(canvasID.substring(7, canvasID.length));

    var bpmnViewer = new BpmnJS({
        container: "#" + canvasID
    });

    canvasElementWithBPMNJS.set(canvasElement, bpmnViewer);

    loadModel(modelID, bpmnViewer);
}

async function loadModel(modelID, bpmnViewer){
    const response = await fetch(`api/bpmnModel?modelID=${modelID}`);
    const blobResponse = await response.blob();
    const xmlString = await blobResponse.text();

    await bpmnViewer.importXML(xmlString);
    // access viewer components
    var canvas = bpmnViewer.get('canvas');
    var overlays = bpmnViewer.get('overlays');

    // zoom to fit full viewport
    canvas.zoom('fit-viewport');
}

function zoomInModel(element){
    canvasElementWithBPMNJS.get(element.parentNode.parentNode).get('zoomScroll').stepZoom(1);
}

function zoomOutModel(element){
    canvasElementWithBPMNJS.get(element.parentNode.parentNode).get('zoomScroll').stepZoom(-1);
}

function zoomFitViewport(element){
    canvasElementWithBPMNJS.get(element.parentNode.parentNode).get('canvas').zoom('fit-viewport');
}
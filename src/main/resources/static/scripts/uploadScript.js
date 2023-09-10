initPage();

var mergeAttributeValid = false;
var waitThresholdValid = false;
function initPage(){
    var page = document.getElementsByClassName("page");
    page[0].style.display = "flex";
    var progressBarItem = document.getElementsByClassName("progressBarNumber")[0];
    progressBarItem.style.backgroundColor = "#017ABF";
}
function showPage(currentPage, newPage){
    var page = document.getElementsByClassName("page");
    page[currentPage - 1].style.display = "none";
    page[newPage - 1].style.display = "flex";

    var progressBarItemPrevious = document.getElementsByClassName("progressBarNumber")[currentPage -1];
    progressBarItemPrevious.style.backgroundColor = "lightgray";
    var progressBarItemNext = document.getElementsByClassName("progressBarNumber")[newPage - 1];
    progressBarItemNext.style.backgroundColor = "#017ABF";
}

// Page: Upload XES-Files
function fileUploaded(){
    const xesFiles = document.getElementById("xesUploadFile");
    var result = "Uploaded files: ";
    for(let i = 0; i < xesFiles.files.length; i++){
        if(i != xesFiles.files.length - 1){
            result = result + xesFiles.files[i].name + ", ";
        }else{
            result = result + xesFiles.files[i].name;
        }
    }
    document.getElementById("uploadedFileNames").innerText = result;
    document.getElementById("nextButton1").disabled = false;
}

// Page: Set Parameter
function validateMergeAttribute(element){
    document.getElementById("nextButton3").disabled = true;
    mergeAttributeValid = false;
    var input = element.value;
    if(input != "" && isNaN(input)){
        mergeAttributeValid = true;
        if(waitThresholdValid == true){
            document.getElementById("nextButton3").disabled = false;
        }
    }
}

function validateWaitThresholdOngoing(element){
    document.getElementById("nextButton3").disabled = true;
    waitThresholdValid = false;
    var input = element.value;
    if(input != "" && !isNaN(input) && input > 0){
        waitThresholdValid = true;
        if(mergeAttributeValid == true){
            document.getElementById("nextButton3").disabled = false;
        }
    }
}

// Page: Confirm & Upload
function activateUploadButton(checkbox){
    var uploadButton = document.getElementsByClassName("uploadButton")[0];
    if(checkbox.checked){
        uploadButton.disabled = false;
    }else{
        uploadButton.disabled = true;
    }
}


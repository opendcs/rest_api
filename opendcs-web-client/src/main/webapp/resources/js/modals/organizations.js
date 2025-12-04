document.addEventListener("DOMContentLoaded", function(event) {
    console.log("organizations js loaded.");
    document.getElementById('organization_select')
        .addEventListener('click', () => {openOrgModal(); });
    document.getElementById("modal_organizations")
        .addEventListener('click', (e) => {
        if (e.target.classList.contains('modal-backdrop')) {
            closeOrgModal();
        }
    });

    document.getElementById("modal_organizations_ok_button")
        .addEventListener('click', () => {
        const selectedId = document.getElementById('id_organization').value;

        if (!selectedId) {
            alert('Please select an organization first.');
            return;
        }
        localStorage.setItem("organizationId", selectedId);
        closeOrgModal();
        window.location.reload();
    });
});

function openOrgModal() {
    const myModal = getOrgModal();
    myModal.show();
    const $orgSelect = $('#id_organization');
    $.ajax({
        url: `${window.API_URL}/organizations`,
        type: "GET",
        dataType: "json",
        success: function (data) {
            data.forEach(function (org) {
                $('<option>')
                    .val(org)
                    .text(org)
                    .appendTo($orgSelect);
            });
            $orgSelect.select2({
                placeholder: 'Select an organization',
                allowClear: true,
                minimumResultsForSearch: 0,
                width: '100%'
            });
            const orgId = localStorage.getItem("organizationId");
            if (orgId && $orgSelect.find('option[value="' + orgId + '"]').length) {
                $orgSelect.val(orgId).trigger('change');
            }
        }
    });
}

function getOrgModal() {
    const modalEl = document.getElementById('modal_organizations');
    let modal = bootstrap.Modal.getInstance(modalEl);
    if (!modal) {
        modal = new bootstrap.Modal(modalEl);
    }
    return modal;
}

function closeOrgModal() {
    const modalEl = document.getElementById('modal_organizations');
    const myModal = new bootstrap.Modal(modalEl);
    myModal.hide();
}
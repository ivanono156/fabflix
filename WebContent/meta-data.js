
// Render metadata to the HTML
function handleMetadata(resultData) {
    let container = jQuery('#metadata-content');
    // Clear previous content
    let tables = resultData["tables"];
    console.log(resultData["tables"]);
    for (let i = 0; i < tables.length; i++) {
        let table = tables[i]; // Get the current table object
        let tableName = table["table_name"]; // Get the table name
        let tableHtml = "<table id='" + tableName + "-table' class='table table-striped metadata-table'>"; // Start the table HTML

        // Add the table name as a header
        //tableHtml += "<caption>" + tableName + "</caption>";
        console.log(table);
        tableHtml += "<h3>" + tableName + "</h3>";
        tableHtml += "<thead><tr>";
        tableHtml += "<th>Attribute</th>";
        tableHtml += "<th>Type</th>";
        tableHtml += "</tr></thead>";
        // Iterate over columns for the current table
        let columns = table.columns;
        tableHtml += "<tbody>";
        console.log(columns);
        for (let j = 0; j < columns.length; j++) {
            let column = columns[j];
            tableHtml += "<tr><td>" + column["column_name"] + "\t"+ "</td><td>" + column["column_type"]+ "</td></tr>"; // Add column data
        }

        tableHtml += "</tbody></table>"; // End the table HTML
        container.append(tableHtml); // Append the table to the container
    }

}

$(document).ready(function() {
    // Ensure your metadata.js logic is here, or in the external file
    $.ajax({
        url: 'api/meta-data',
        method: 'GET',
        success: function(resultData) {
            handleMetadata(resultData);
        },
        error: function(err) {
            console.log('Error loading metadata:', err);
        }
    });
});

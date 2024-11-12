# Test descriptions

Tests have been made from test set for CSV on the Web Validators available at CSVW RDF Tests (270
entries) https://w3c.github.io/csvw/tests/#manifest-rdf. The tests have been reversed, taking the resulting RDF and
expecting similar result as was the original CSV for the test.

Because of the reverse technique, not all of the tests were eligible to be reproduced outright. Especially the negative
tests, that did not produce RDF in the original test. Those negative tests will have a new test set ready for them.

This folder contains test sources for positive tests.

The tests are testing the following:

manifest-rdf#test001: Simple table

    The simplest possible table without metadata

manifest-rdf#test005: Identifier references

    A table with entity identifiers and references to other entities without metadata

manifest-rdf#test006: No identifiers

    Records contain two entities with relationships which are duplicated without metadata

manifest-rdf#test007: Joined table with unique identifiers

    Joined data with identified records without metadata

manifest-rdf#test008: Microsyntax - internal field separator

    One field has comma-separated values without metadata

manifest-rdf#test009: Microsyntax - formatted time

    Field with parseable human formatted time without metadata

manifest-rdf#test010: Country-codes-and-names example

    Country-codes-and-names example

manifest-rdf#test011: tree-ops example with metadata

    Processors MUST use the first metadata found for processing a tabular data file by using overriding metadata, if provided. Otherwise processors MUST attempt to locate the first metadata document from the Link header or the metadata located through site-wide configuration.

manifest-rdf#test012: tree-ops example with directory metadata

    Processors MUST use the first metadata found for processing a tabular data file by using overriding metadata, if provided. Otherwise processors MUST attempt to locate the first metadata document from the Link header or the metadata located through site-wide configuration.

manifest-rdf#test013: tree-ops example from user metadata

    Processors MUST use the first metadata found for processing a tabular data file by using overriding metadata, if provided. Otherwise processors MUST attempt to locate the first metadata document from the Link header or the metadata located through site-wide configuration.

manifest-rdf#test014: tree-ops example with linked metadata

    Processors MUST use the first metadata found for processing a tabular data file by using overriding metadata, if provided. Otherwise processors MUST attempt to locate the first metadata document from the Link header or the metadata located through site-wide configuration.

manifest-rdf#test015: tree-ops example with user and directory metadata

    Processors MUST use the first metadata found for processing a tabular data file by using overriding metadata, if provided. Otherwise processors MUST attempt to locate the first metadata document from the Link header or the metadata located through site-wide configuration.

manifest-rdf#test016: tree-ops example with linked and directory metadata

    Processors MUST use the first metadata found for processing a tabular data file by using overriding metadata, if provided. Otherwise processors MUST attempt to locate the first metadata document from the Link header or the metadata located through site-wide configuration.

manifest-rdf#test017: tree-ops example with file and directory metadata

    Processors MUST use the first metadata found for processing a tabular data file by using overriding metadata, if provided. Otherwise processors MUST attempt to locate the first metadata document from the Link header or the metadata located through site-wide configuration.

manifest-rdf#test018: tree-ops example with user, file and directory metadata

    Processors MUST use the first metadata found for processing a tabular data file by using overriding metadata, if provided. Otherwise processors MUST attempt to locate the first metadata document from the Link header or the metadata located through site-wide configuration.

manifest-rdf#test023: dialect: header=false

    If true, sets the header row count flag to 1, and if false to 0, unless headerRowCount is provided, in which case the value provided for the header property is ignored.

manifest-rdf#test027: tree-ops minimal output

    Processors MUST use the first metadata found for processing a tabular data file by using overriding metadata, if provided. Otherwise processors MUST attempt to locate the first metadata document from the Link header or the metadata located through site-wide configuration.

manifest-rdf#test028: countries.csv example

    If no metadata is supplied or found, processors MUST use embedded metadata.

manifest-rdf#test029: countries.csv minimal

    If no metadata is supplied or found, processors MUST use embedded metadata.

manifest-rdf#test030: countries.json example

    countries.json from metadata

manifest-rdf#test031: countries.json example minimal output

    countries.json from metadata minimal output

manifest-rdf#test032: events-listing.csv example

    events-listing example from metadata, virtual columns and multiple subjects per row

manifest-rdf#test033: events-listing.csv minimal output

    events-listing example from metadata, virtual columns and multiple subjects per row; minimal output

manifest-rdf#test034: roles example

    Public Sector Roles example with referenced schemas. Validation fails because organization.csv intentionally contains an invalid reference.

manifest-rdf#test035: roles minimal

    Public Sector Roles example with referenced schemas; minimal output. Validation fails because organization.csv intentionally contains an invalid reference.

manifest-rdf#test036: tree-ops-ext example

    tree-ops extended example

manifest-rdf#test037: tree-ops-ext minimal

    tree-ops extended example; minimal output

manifest-rdf#test038: inherited properties propagation

    Setting inherited properties at different levels inherit to cell

manifest-rdf#test039: valid inherited properties

    Different combinations of valid inherited properties

manifest-rdf#test040: invalid null

    If a property has a value that is not permitted by this specification, then if a default value is provided for that property, compliant applications MUST use that default value and MUST generate a warning. If no default value is provided for that property, compliant applications MUST generate a warning and behave as if the property had not been specified.

manifest-rdf#test041: invalid lang

    If a property has a value that is not permitted by this specification, then if a default value is provided for that property, compliant applications MUST use that default value and MUST generate a warning. If no default value is provided for that property, compliant applications MUST generate a warning and behave as if the property had not been specified.

manifest-rdf#test042: invalid textDirection

    If a property has a value that is not permitted by this specification, then if a default value is provided for that property, compliant applications MUST use that default value and MUST generate a warning. If no default value is provided for that property, compliant applications MUST generate a warning and behave as if the property had not been specified.

manifest-rdf#test043: invalid separator

    If a property has a value that is not permitted by this specification, then if a default value is provided for that property, compliant applications MUST use that default value and MUST generate a warning. If no default value is provided for that property, compliant applications MUST generate a warning and behave as if the property had not been specified.

manifest-rdf#test044: invalid ordered

    If a property has a value that is not permitted by this specification, then if a default value is provided for that property, compliant applications MUST use that default value and MUST generate a warning. If no default value is provided for that property, compliant applications MUST generate a warning and behave as if the property had not been specified.

manifest-rdf#test045: invalid default

    If a property has a value that is not permitted by this specification, then if a default value is provided for that property, compliant applications MUST use that default value and MUST generate a warning. If no default value is provided for that property, compliant applications MUST generate a warning and behave as if the property had not been specified.

manifest-rdf#test046: invalid dataype

    If a property has a value that is not permitted by this specification, then if a default value is provided for that property, compliant applications MUST use that default value and MUST generate a warning. If no default value is provided for that property, compliant applications MUST generate a warning and behave as if the property had not been specified.

manifest-rdf#test047: invalid aboutUrl

    If a property has a value that is not permitted by this specification, then if a default value is provided for that property, compliant applications MUST use that default value and MUST generate a warning. If no default value is provided for that property, compliant applications MUST generate a warning and behave as if the property had not been specified.

manifest-rdf#test048: invalid propertyUrl

    If a property has a value that is not permitted by this specification, then if a default value is provided for that property, compliant applications MUST use that default value and MUST generate a warning. If no default value is provided for that property, compliant applications MUST generate a warning and behave as if the property had not been specified.

manifest-rdf#test049: invalid valueUrl

    If a property has a value that is not permitted by this specification, then if a default value is provided for that property, compliant applications MUST use that default value and MUST generate a warning. If no default value is provided for that property, compliant applications MUST generate a warning and behave as if the property had not been specified.

manifest-rdf#test059: dialect: invalid commentPrefix

    If a property has a value that is not permitted by this specification, then if a default value is provided for that property, compliant applications MUST use that default value and MUST generate a warning. If no default value is provided for that property, compliant applications MUST generate a warning and behave as if the property had not been specified.

manifest-rdf#test060: dialect: invalid delimiter

    If a property has a value that is not permitted by this specification, then if a default value is provided for that property, compliant applications MUST use that default value and MUST generate a warning. If no default value is provided for that property, compliant applications MUST generate a warning and behave as if the property had not been specified.

manifest-rdf#test061: dialect: invalid doubleQuote

    If a property has a value that is not permitted by this specification, then if a default value is provided for that property, compliant applications MUST use that default value and MUST generate a warning. If no default value is provided for that property, compliant applications MUST generate a warning and behave as if the property had not been specified.

manifest-rdf#test062: dialect: invalid encoding

    If a property has a value that is not permitted by this specification, then if a default value is provided for that property, compliant applications MUST use that default value and MUST generate a warning. If no default value is provided for that property, compliant applications MUST generate a warning and behave as if the property had not been specified.

manifest-rdf#test063: dialect: invalid header

    If a property has a value that is not permitted by this specification, then if a default value is provided for that property, compliant applications MUST use that default value and MUST generate a warning. If no default value is provided for that property, compliant applications MUST generate a warning and behave as if the property had not been specified.

manifest-rdf#test065: dialect: invalid headerRowCount

    If a property has a value that is not permitted by this specification, then if a default value is provided for that property, compliant applications MUST use that default value and MUST generate a warning. If no default value is provided for that property, compliant applications MUST generate a warning and behave as if the property had not been specified.

manifest-rdf#test066: dialect: invalid lineTerminators

    If a property has a value that is not permitted by this specification, then if a default value is provided for that property, compliant applications MUST use that default value and MUST generate a warning. If no default value is provided for that property, compliant applications MUST generate a warning and behave as if the property had not been specified.

manifest-rdf#test067: dialect: invalid quoteChar

    If a property has a value that is not permitted by this specification, then if a default value is provided for that property, compliant applications MUST use that default value and MUST generate a warning. If no default value is provided for that property, compliant applications MUST generate a warning and behave as if the property had not been specified.

manifest-rdf#test068: dialect: invalid skipBlankRows

    If a property has a value that is not permitted by this specification, then if a default value is provided for that property, compliant applications MUST use that default value and MUST generate a warning. If no default value is provided for that property, compliant applications MUST generate a warning and behave as if the property had not been specified.

manifest-rdf#test069: dialect: invalid skipColumns

    If a property has a value that is not permitted by this specification, then if a default value is provided for that property, compliant applications MUST use that default value and MUST generate a warning. If no default value is provided for that property, compliant applications MUST generate a warning and behave as if the property had not been specified.

manifest-rdf#test070: dialect: invalid skipInitialSpace

    If a property has a value that is not permitted by this specification, then if a default value is provided for that property, compliant applications MUST use that default value and MUST generate a warning. If no default value is provided for that property, compliant applications MUST generate a warning and behave as if the property had not been specified.

manifest-rdf#test071: dialect: invalid skipRows

    If a property has a value that is not permitted by this specification, then if a default value is provided for that property, compliant applications MUST use that default value and MUST generate a warning. If no default value is provided for that property, compliant applications MUST generate a warning and behave as if the property had not been specified.

manifest-rdf#test072: dialect: invalid trim

    If a property has a value that is not permitted by this specification, then if a default value is provided for that property, compliant applications MUST use that default value and MUST generate a warning. If no default value is provided for that property, compliant applications MUST generate a warning and behave as if the property had not been specified.

manifest-rdf#test073: invalid @language

    The value of @language MUST be a valid BCP47 language code

manifest-rdf#test074: empty tables

    Compliant application MUST raise an error if this array does not contain one or more table descriptions.

    Negative Test

manifest-rdf#test075: invalid tableGroup tableDirection

    An atomic property that MUST have a single string value that is one of "rtl", "ltr" or "auto".

manifest-rdf#test076: invalid table tableDirection

    An atomic property that MUST have a single string value that is one of "rtl", "ltr" or "auto".

manifest-rdf#test077: invalid tableGroup @id

    It MUST NOT start with _:.

    Negative Test

manifest-rdf#test078: invalid table @id

    It MUST NOT start with _:.

    Negative Test

manifest-rdf#test079: invalid schema @id

    It MUST NOT start with _:.

    Negative Test

manifest-rdf#test080: invalid column @id

    It MUST NOT start with _:.

    Negative Test

manifest-rdf#test081: invalid dialect @id

    It MUST NOT start with _:.

    Negative Test

manifest-rdf#test082: invalid template @id

    It MUST NOT start with _:.

    Negative Test

manifest-rdf#test083: invalid tableGroup @type

    If included @type MUST be TableGroup

    Negative Test

manifest-rdf#test084: invalid table @type

    If included @type MUST be TableGroup

    Negative Test

manifest-rdf#test085: invalid schema @type

    If included @type MUST be TableGroup

    Negative Test

manifest-rdf#test086: invalid column @type

    If included @type MUST be TableGroup

    Negative Test

manifest-rdf#test087: invalid dialect @type

    If included @type MUST be Dialect

    Negative Test

manifest-rdf#test088: invalid transformation @type

    If included @type MUST be Template

    Negative Test

manifest-rdf#test089: missing tables in TableGroup

    The tables property is required in a TableGroup

    Negative Test

manifest-rdf#test090: missing url in Table

    The url property is required in a Table

    Negative Test

manifest-rdf#test093: undefined properties

    Compliant applications MUST ignore properties (aside from common properties) which are not defined in this specification and MUST generate a warning when they are encoutered

manifest-rdf#test095: inconsistent array values: transformations

    Any items within an array that are not valid objects of the type expected are ignored

manifest-rdf#test097: inconsistent array values: foreignKeys

    Any items within an array that are not valid objects of the type expected are ignored

manifest-rdf#test098: inconsistent array values: tables

    If the supplied value of an array property is not an array (eg if it is an integer), compliant applications MUST issue a warning and proceed as if the property had been supplied with an empty array. Compliant application MUST raise an error if this array does not contain one or more table descriptions.

    Negative Test

manifest-rdf#test099: inconsistent array values: transformations

    If the supplied value of an array property is not an array (eg if it is an integer), compliant applications MUST issue a warning and proceed as if the property had been supplied with an empty array

manifest-rdf#test100: inconsistent array values: columns

    If the supplied value of an array property is not an array (eg if it is an integer), compliant applications MUST issue a warning and proceed as if the property had been supplied with an empty array

manifest-rdf#test101: inconsistent array values: foreignKeys

    If the supplied value of an array property is not an array (eg if it is an integer), compliant applications MUST issue a warning and proceed as if the property had been supplied with an empty array

manifest-rdf#test102: inconsistent link values: @id

    If the supplied value of an array property is not an array (eg if it is an integer), compliant applications MUST issue a warning and proceed as if the property had been supplied with an empty array

manifest-rdf#test103: inconsistent link values: url

    If the supplied value of an array property is not an array (eg if it is an integer), compliant applications MUST issue a warning and proceed as if the property had been supplied with an empty array

    Negative Test

manifest-rdf#test104: invalid columnReference

    The referenced description object MUST have a name property

    Negative Test

manifest-rdf#test105: invalid primaryKey

    The referenced description object MUST have a name property

manifest-rdf#test106: invalid dialect

    If the supplied value of an object property is not a string or object (eg if it is an integer), compliant applications MUST issue a warning and proceed as if the property had been specified as an object with no properties.

manifest-rdf#test107: invalid tableSchema

    If the supplied value of an object property is not a string or object (eg if it is an integer), compliant applications MUST issue a warning and proceed as if the property had been specified as an object with no properties.

manifest-rdf#test108: invalid reference

    If the supplied value of an object property is not a string or object (eg if it is an integer), compliant applications MUST issue a warning and proceed as if the property had been specified as an object with no properties.

    Negative Test

manifest-rdf#test109: titles with invalid language

    Natural Language properties may be objects whose properties MUST be language codes as defined by [BCP47] and whose values are either strings or arrays, providing natural language strings in that language. Validation fails because without a title, the metadata is incompatible with the CSV, which isn't a problem when not validating.

manifest-rdf#test110: titles with non-string values

    Natural Language properties may be objects whose properties MUST be language codes as defined by [BCP47] and whose values are either strings or arrays, providing natural language strings in that language

manifest-rdf#test111: titles with invalid value

    If the supplied value of a natural language property is not a string, array or object (eg if it is an integer), compliant applications MUST issue a warning and proceed as if the property had been specified as an empty array. Validation fails because without a title, the metadata is incompatible with the CSV, which isn't a problem when not validating.

manifest-rdf#test112: titles with non-string array values

    If the supplied value is an array, any items in that array that are not strings MUST be ignored

manifest-rdf#test113: invalid suppressOutput

    Atomic properties: Processors MUST issue a warning if a property is set to an invalid value type

manifest-rdf#test114: invalid name

    Atomic properties: Processors MUST issue a warning if a property is set to an invalid value type

manifest-rdf#test115: invalid virtual

    Atomic properties: Processors MUST issue a warning if a property is set to an invalid value type

manifest-rdf#test116: file-metadata with query component not found

    processors MUST attempt to locate a metadata documents through site-wide configuration.

manifest-rdf#test117: file-metadata not referencing file

    If the metadata file found at this location does not explicitly include a reference to the requested tabular data file then it MUST be ignored.

manifest-rdf#test118: directory-metadata with query component

    processors MUST attempt to locate a metadata documents through site-wide configuration. component.

manifest-rdf#test119: directory-metadata not referencing file

    If the metadata file found at this location does not explicitly include a reference to the requested tabular data file then it MUST be ignored.

manifest-rdf#test120: link-metadata not referencing file

    If the metadata file found at this location does not explicitly include a reference to the requested tabular data file then it MUST be ignored.

manifest-rdf#test121: user-metadata not referencing file

    User-specified metadata does not need to reference the starting CSV

    type
        csvt:ToRdfTest
    approval
        rdft:Approved
    action
        test121.csv 
    result
        test121.ttl 
    Implicit
        test121-ref.csv test121-user-metadata.json 
    options
        noProv: true user metadata: test121-user-metadata.json 

manifest-rdf#test122: link-metadata not describing file uses file-metadata

    If the metadata file found at this location does not explicitly include a reference to the requested tabular data file then it MUST be ignored.

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test122.csv 
    result
        test122.ttl 
    Link Header
        <test122-linked-metadata.json>; rel="describedby"; type="application/csvm+json" 
    Implicit
        test122.csv-metadata.json test122-linked-metadata.json 
    options
        noProv: true 

manifest-rdf#test123: file-metadata not describing file uses directory-metadata

    If the metadata file found at this location does not explicitly include a reference to the requested tabular data file then it MUST be ignored.

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test123/action.csv 
    result
        test123/result.ttl 
    Implicit
        test123/action.csv-metadata.json test123/csv-metadata.json 
    options
        noProv: true 

manifest-rdf#test124: metadata with columns not matching csv titles

    If not validating, and one schema has a name property but not a titles property, and the other has a titles property but not a name property.

    type
        csvt:ToRdfTest
    approval
        rdft:Approved
    action
        tree-ops.csv 
    result
        test124.ttl 
    Implicit
        test124-user-metadata.json 
    options
        noProv: true user metadata: test124-user-metadata.json 

manifest-rdf#test125: required column with empty cell

    If the column required annotation is true, add an error to the list of errors for the cell.

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test125-metadata.json 
    result
        test125.ttl 
    Implicit
        test125.csv 
    options
        noProv: true 

manifest-rdf#test126: required column with cell matching null

    if the string is the same as any one of the values of the column null annotation, then the resulting value is null. If the column separator annotation is null and the column required annotation is true, add an error to the list of errors for the cell.

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test126-metadata.json 
    result
        test126.ttl 
    Implicit
        test126.csv 
    options
        noProv: true 

manifest-rdf#test127: incompatible table

    if TM is not compatible with EM validators MUST raise an error, other processors MUST generate a warning and continue processing

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test127-metadata.json 
    result
        test127.ttl 
    Implicit
        test127.csv 
    options
        noProv: true 

manifest-rdf#test128: duplicate column names

    The name properties of the column descriptions MUST be unique within a given table description.

    Negative Test

    type
        csvt:NegativeRdfTest
    approval
        rdft:Approved
    action
        test128-metadata.json 
    Implicit
        tree-ops.csv 
    options
        noProv: true 

manifest-rdf#test129: columnn name as integer

    This (name) MUST be a string and this property has no default value, which means it MUST be ignored if the supplied value is not a string.

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test129-metadata.json 
    result
        test129.ttl 
    Implicit
        tree-ops.csv 
    options
        noProv: true 

manifest-rdf#test130: invalid column name

    column names are restricted as defined in Variables in [URI-TEMPLATE]

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test130-metadata.json 
    result
        test130.ttl 
    Implicit
        tree-ops.csv 
    options
        noProv: true 

manifest-rdf#test131: invalid column name

    column names are restricted ... names beginning with '_' are reserved by this specification and MUST NOT be used within metadata documents.

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test131-metadata.json 
    result
        test131.ttl 
    Implicit
        tree-ops.csv 
    options
        noProv: true 

manifest-rdf#test132: name annotation from title percent encoded

    If there is no name property defined on this column, the first titles value having the same language tag as default language, or und or if no default language is specified, becomes the name annotation for the described column. This annotation MUST be percent-encoded as necessary to conform to the syntactic requirements defined in [RFC3986]

    type
        csvt:ToRdfTest
    approval
        rdft:Approved
    action
        test132-metadata.json 
    result
        test132.ttl 
    Implicit
        tree-ops.csv 
    options
        noProv: true 

manifest-rdf#test133: virtual before non-virtual

    If present, a virtual column MUST appear after all other non-virtual column definitions.

    Negative Test

    type
        csvt:NegativeRdfTest
    approval
        rdft:Approved
    action
        test133-metadata.json 
    Implicit
        tree-ops.csv 
    options
        noProv: true 

manifest-rdf#test134: context in common property

    A metadata document MUST NOT add a new context

    Negative Test

    type
        csvt:NegativeRdfTest
    approval
        rdft:Approved
    action
        test134-metadata.json 
    Implicit
        tree-ops.csv 
    options
        noProv: true 

manifest-rdf#test135: @list value

    Values MUST NOT use list objects or set objects.

    Negative Test

    type
        csvt:NegativeRdfTest
    approval
        rdft:Approved
    action
        test135-metadata.json 
    Implicit
        tree-ops.csv 
    options
        noProv: true 

manifest-rdf#test136: @set value

    Values MUST NOT use list objects or set objects.

    Negative Test

    type
        csvt:NegativeRdfTest
    approval
        rdft:Approved
    action
        test136-metadata.json 
    Implicit
        tree-ops.csv 
    options
        noProv: true 

manifest-rdf#test137: @type out of range (as datatype)

    The value of any @id or @type contained within a metadata document MUST NOT be a blank node.

    Negative Test

    type
        csvt:NegativeRdfTest
    approval
        rdft:Approved
    action
        test137-metadata.json 
    Implicit
        tree-ops.csv 
    options
        noProv: true 

manifest-rdf#test138: @type out of range (as node type)

    The value of any @id or @type contained within a metadata document MUST NOT be a blank node.

    Negative Test

    type
        csvt:NegativeRdfTest
    approval
        rdft:Approved
    action
        test138-metadata.json 
    Implicit
        tree-ops.csv 
    options
        noProv: true 

manifest-rdf#test139: @type out of range (as node type) - string

    The value of any member of @type MUST be either a term defined in [csvw-context], a prefixed name where the prefix is a term defined in [csvw-context], or an absolute URL.

    Negative Test

    type
        csvt:NegativeRdfTest
    approval
        rdft:Approved
    action
        test139-metadata.json 
    Implicit
        tree-ops.csv 
    options
        noProv: true 

manifest-rdf#test140: @type out of range (as node type) - integer

    The value of any member of @type MUST be either a term defined in [csvw-context], a prefixed name where the prefix is a term defined in [csvw-context], or an absolute URL.

    Negative Test

    type
        csvt:NegativeRdfTest
    approval
        rdft:Approved
    action
        test140-metadata.json 
    Implicit
        tree-ops.csv 
    options
        noProv: true 

manifest-rdf#test141: @id out of range (as node type) - bnode

    The value of any @id or @type contained within a metadata document MUST NOT be a blank node.

    Negative Test

    type
        csvt:NegativeRdfTest
    approval
        rdft:Approved
    action
        test141-metadata.json 
    Implicit
        tree-ops.csv 
    options
        noProv: true 

manifest-rdf#test142: @value with @language and @type

    If a @value property is used on an object, that object MUST NOT have any other properties aside from either @type or @language, and MUST NOT have both @type and @language as properties. The value of the @value property MUST be a string, number, or boolean value.

    Negative Test

    type
        csvt:NegativeRdfTest
    approval
        rdft:Approved
    action
        test142-metadata.json 
    Implicit
        tree-ops.csv 
    options
        noProv: true 

manifest-rdf#test143: @value with extra properties

    If a @value property is used on an object, that object MUST NOT have any other properties aside from either @type or @language, and MUST NOT have both @type and @language as properties. The value of the @value property MUST be a string, number, or boolean value.

    Negative Test

    type
        csvt:NegativeRdfTest
    approval
        rdft:Approved
    action
        test143-metadata.json 
    Implicit
        tree-ops.csv 
    options
        noProv: true 

manifest-rdf#test144: @language outside of @value

    A @language property MUST NOT be used on an object unless it also has a @value property.

    Negative Test

    type
        csvt:NegativeRdfTest
    approval
        rdft:Approved
    action
        test144-metadata.json 
    Implicit
        tree-ops.csv 
    options
        noProv: true 

manifest-rdf#test146: Invalid faux-keyword

    Aside from @value, @type, @language, and @id, the properties used on an object MUST NOT start with @.

    Negative Test

    type
        csvt:NegativeRdfTest
    approval
        rdft:Approved
    action
        test146-metadata.json 
    Implicit
        tree-ops.csv 
    options
        noProv: true 

manifest-rdf#test147: title incompatible with title on case

    If there is a non-empty case-sensitive intersection between the titles values, where matches MUST have a matching language; und matches any language, and languages match if they are equal when truncated, as defined in [BCP47], to the length of the shortest language tag.

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test147-metadata.json 
    result
        test147.ttl 
    Implicit
        tree-ops.csv 
    options
        noProv: true 

manifest-rdf#test148: title incompatible with title on language

    If there is a non-empty case-sensitive intersection between the titles values, where matches MUST have a matching language; und matches any language, and languages match if they are equal when truncated, as defined in [BCP47], to the length of the shortest language tag.

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test148-metadata.json 
    result
        test148.ttl 
    Implicit
        tree-ops.csv 
    options
        noProv: true 

manifest-rdf#test149: title compatible with title on less specific language

    If there is a non-empty case-sensitive intersection between the titles values, where matches MUST have a matching language; und matches any language, and languages match if they are equal when truncated, as defined in [BCP47], to the length of the shortest language tag.

    type
        csvt:ToRdfTest
    approval
        rdft:Approved
    action
        test149-metadata.json 
    result
        test149.ttl 
    Implicit
        tree-ops.csv 
    options
        noProv: true 

manifest-rdf#test150: non-builtin datatype (datatype value)

    If the value of this property is a string, it MUST be one of the built-in datatypes defined in section 5.11.1 Built-in Datatypes or an absolute URL

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test150-metadata.json 
    result
        test150.ttl 
    Implicit
        tree-ops.csv 
    options
        noProv: true 

manifest-rdf#test151: non-builtin datatype (base value)

    If the value of this property is a string, it MUST be one of the built-in datatypes

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test151-metadata.json 
    result
        test151.ttl 
    Implicit
        tree-ops.csv 
    options
        noProv: true 

manifest-rdf#test152: string format (valid combinations)

    If the datatype base is not numeric, boolean, a date/time type, or a duration type, the datatype format annotation provides a regular expression for the string values

    type
        csvt:ToRdfTest
    approval
        rdft:Approved
    action
        test152-metadata.json 
    result
        test152.ttl 
    Implicit
        test152.csv 
    options
        noProv: true 

manifest-rdf#test153: string format (bad format string)

    If the datatype base is not numeric, boolean, a date/time type, or a duration type, the datatype format annotation provides a regular expression for the string values

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test153-metadata.json 
    result
        test153.ttl 
    Implicit
        test153.csv 
    options
        noProv: true 

manifest-rdf#test154: string format (value not matching format)

    If the datatype base is not numeric, boolean, a date/time type, or a duration type, the datatype format annotation provides a regular expression for the string values

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test154-metadata.json 
    result
        test154.ttl 
    Implicit
        test154.csv 
    options
        noProv: true 

manifest-rdf#test155: number format (valid combinations)

    If the datatype format annotation is a single string, this is interpreted in the same way as if it were an object with a pattern property whose value is that string

    type
        csvt:ToRdfTest
    approval
        rdft:Approved
    action
        test155-metadata.json 
    result
        test155.ttl 
    Implicit
        test155.csv 
    options
        noProv: true 

manifest-rdf#test156: number format (bad format string)

    If the datatype format annotation is a single string, this is interpreted in the same way as if it were an object with a pattern property whose value is that string

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test156-metadata.json 
    result
        test156.ttl 
    Implicit
        test156.csv 
    options
        noProv: true 

manifest-rdf#test157: number format (value not matching format)

    If the datatype format annotation is a single string, this is interpreted in the same way as if it were an object with a pattern property whose value is that string

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test157-metadata.json 
    result
        test157.ttl 
    Implicit
        test157.csv 
    options
        noProv: true 

manifest-rdf#test158: number format (valid combinations)

    Numeric dataype with object format

    type
        csvt:ToRdfTest
    approval
        rdft:Approved
    action
        test158-metadata.json 
    result
        test158.ttl 
    Implicit
        test158.csv 
    options
        noProv: true 

manifest-rdf#test159: number format (bad pattern format string)

    If the datatype format annotation is a single string, this is interpreted in the same way as if it were an object with a pattern property whose value is that string

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test159-metadata.json 
    result
        test159.ttl 
    Implicit
        test159.csv 
    options
        noProv: true 

manifest-rdf#test160: number format (not matching values with pattern)

    Implementations MUST add a validation error to the errors annotation for the cell if the string being parsed

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test160-metadata.json 
    result
        test160.ttl 
    Implicit
        test160.csv 
    options
        noProv: true 

manifest-rdf#test161: number format (not matching values without pattern)

    Implementations MUST add a validation error to the errors annotation for the cell if the string being parsed

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test161-metadata.json 
    result
        test161.ttl 
    Implicit
        test161.csv 
    options
        noProv: true 

manifest-rdf#test162: numeric format (consecutive groupChar)

    Implementations MUST add a validation error to the errors annotation for the cell if the string being parsed contains two consecutive groupChar strings

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test162-metadata.json 
    result
        test162.ttl 
    Implicit
        test162.csv 
    options
        noProv: true 

manifest-rdf#test163: integer datatype with decimalChar

    Implementations MUST add a validation error to the errors annotation for the cell if the string being parsed contains the decimalChar, if the datatype base is integer or one of its sub-values

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test163-metadata.json 
    result
        test163.ttl 
    Implicit
        test163.csv 
    options
        noProv: true 

manifest-rdf#test164: decimal datatype with exponent

    Implementations MUST add a validation error to the errors annotation for the cell contains an exponent, if the datatype base is decimal or one of its sub-values

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test164-metadata.json 
    result
        test164.ttl 
    Implicit
        test164.csv 
    options
        noProv: true 

manifest-rdf#test165: decimal type with NaN

    Implementations MUST add a validation error to the errors annotation for the cell contains an exponent, is one of the special values NaN, INF, or -INF, if the datatype base is decimal or one of its sub-values

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test165-metadata.json 
    result
        test165.ttl 
    Implicit
        test165.csv 
    options
        noProv: true 

manifest-rdf#test166: decimal type with INF

    Implementations MUST add a validation error to the errors annotation for the cell contains an exponent, is one of the special values NaN, INF, or -INF, if the datatype base is decimal or one of its sub-values

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test166-metadata.json 
    result
        test166.ttl 
    Implicit
        test166.csv 
    options
        noProv: true 

manifest-rdf#test167: decimal type with -INF

    Implementations MUST add a validation error to the errors annotation for the cell contains an exponent, is one of the special values NaN, INF, or -INF, if the datatype base is decimal or one of its sub-values

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test167-metadata.json 
    result
        test167.ttl 
    Implicit
        test167.csv 
    options
        noProv: true 

manifest-rdf#test168: decimal with implicit groupChar

    When parsing the string value of a cell against this format specification, implementations MUST recognise and parse numbers

    type
        csvt:ToRdfTest
    approval
        rdft:Approved
    action
        test168-metadata.json 
    result
        test168.ttl 
    Implicit
        test168.csv 
    options
        noProv: true 

manifest-rdf#test169: invalid decimal

    Implementations MUST add a validation error to the errors annotation for the cell contains an exponent, does not meet the numeric format defined above

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test169-metadata.json 
    result
        test169.ttl 
    Implicit
        test169.csv 
    options
        noProv: true 

manifest-rdf#test170: decimal with percent

    Implementations MUST use the sign, exponent, percent, and per-mille signs when parsing the string value of a cell to provide the value of the cell

    type
        csvt:ToRdfTest
    approval
        rdft:Approved
    action
        test170-metadata.json 
    result
        test170.ttl 
    Implicit
        test170.csv 
    options
        noProv: true 

manifest-rdf#test171: decimal with per-mille

    Implementations MUST use the sign, exponent, percent, and per-mille signs when parsing the string value of a cell to provide the value of the cell

    type
        csvt:ToRdfTest
    approval
        rdft:Approved
    action
        test171-metadata.json 
    result
        test171.ttl 
    Implicit
        test171.csv 
    options
        noProv: true 

manifest-rdf#test172: invalid byte

    Implementations MUST add a validation error to the errors annotation for the cell contains an exponent, does not meet the numeric format defined above

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test172-metadata.json 
    result
        test172.ttl 
    Implicit
        test172.csv 
    options
        noProv: true 

manifest-rdf#test173: invald unsignedLong

    Implementations MUST add a validation error to the errors annotation for the cell contains an exponent, does not meet the numeric format defined above

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test173-metadata.json 
    result
        test173.ttl 
    Implicit
        test173.csv 
    options
        noProv: true 

manifest-rdf#test174: invalid unsignedShort

    Implementations MUST add a validation error to the errors annotation for the cell contains an exponent, does not meet the numeric format defined above

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test174-metadata.json 
    result
        test174.ttl 
    Implicit
        test174.csv 
    options
        noProv: true 

manifest-rdf#test175: invalid unsignedByte

    Implementations MUST add a validation error to the errors annotation for the cell contains an exponent, does not meet the numeric format defined above

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test175-metadata.json 
    result
        test175.ttl 
    Implicit
        test175.csv 
    options
        noProv: true 

manifest-rdf#test176: invalid positiveInteger

    Implementations MUST add a validation error to the errors annotation for the cell contains an exponent, does not meet the numeric format defined above

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test176-metadata.json 
    result
        test176.ttl 
    Implicit
        test176.csv 
    options
        noProv: true 

manifest-rdf#test177: invalid negativeInteger

    Implementations MUST add a validation error to the errors annotation for the cell contains an exponent, does not meet the numeric format defined above

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test177-metadata.json 
    result
        test177.ttl 
    Implicit
        test177.csv 
    options
        noProv: true 

manifest-rdf#test178: invalid nonPositiveInteger

    Implementations MUST add a validation error to the errors annotation for the cell contains an exponent, does not meet the numeric format defined above

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test178-metadata.json 
    result
        test178.ttl 
    Implicit
        test178.csv 
    options
        noProv: true 

manifest-rdf#test179: invalid nonNegativeInteger

    Implementations MUST add a validation error to the errors annotation for the cell contains an exponent, does not meet the numeric format defined above

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test179-metadata.json 
    result
        test179.ttl 
    Implicit
        test179.csv 
    options
        noProv: true 

manifest-rdf#test180: invalid double

    Implementations MUST add a validation error to the errors annotation for the cell contains an exponent, does not meet the numeric format defined above

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test180-metadata.json 
    result
        test180.ttl 
    Implicit
        test180.csv 
    options
        noProv: true 

manifest-rdf#test181: invalid number

    Implementations MUST add a validation error to the errors annotation for the cell contains an exponent, does not meet the numeric format defined above

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test181-metadata.json 
    result
        test181.ttl 
    Implicit
        test181.csv 
    options
        noProv: true 

manifest-rdf#test182: invalid float

    Implementations MUST add a validation error to the errors annotation for the cell contains an exponent, does not meet the numeric format defined above

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test182-metadata.json 
    result
        test182.ttl 
    Implicit
        test182.csv 
    options
        noProv: true 

manifest-rdf#test183: boolean format (valid combinations)

    If the datatype base for a cell is boolean, the datatype format annotation provides the true and false values expected, separated by |.

    type
        csvt:ToRdfTest
    approval
        rdft:Approved
    action
        test183-metadata.json 
    result
        test183.ttl 
    Implicit
        test183.csv 
    options
        noProv: true 

manifest-rdf#test184: boolean format (bad format string)

    If the datatype base for a cell is boolean, the datatype format annotation provides the true and false values expected, separated by |.

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test184-metadata.json 
    result
        test184.ttl 
    Implicit
        test184.csv 
    options
        noProv: true 

manifest-rdf#test185: boolean format (value not matching format)

    If the datatype base for a cell is boolean, the datatype format annotation provides the true and false values expected, separated by |.

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test185-metadata.json 
    result
        test185.ttl 
    Implicit
        test185.csv 
    options
        noProv: true 

manifest-rdf#test186: boolean format (not matching datatype)

    Implementations MUST add a validation error to the errors annotation for the cell if the string being parsed

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test186-metadata.json 
    result
        test186.ttl 
    Implicit
        test186.csv 
    options
        noProv: true 

manifest-rdf#test187: date format (valid native combinations)

    The supported date and time formats listed here are expressed in terms of the date field symbols defined in [UAX35] and MUST be interpreted by implementations as defined in that specification.

    type
        csvt:ToRdfTest
    approval
        rdft:Approved
    action
        test187-metadata.json 
    result
        test187.ttl 
    Implicit
        test187.csv 
    options
        noProv: true 

manifest-rdf#test188: date format (valid date combinations with formats)

    The supported date and time formats listed here are expressed in terms of the date field symbols defined in [UAX35] and MUST be interpreted by implementations as defined in that specification.

    type
        csvt:ToRdfTest
    approval
        rdft:Approved
    action
        test188-metadata.json 
    result
        test188.ttl 
    Implicit
        test188.csv 
    options
        noProv: true 

manifest-rdf#test189: date format (valid time combinations with formats)

    The supported date and time formats listed here are expressed in terms of the date field symbols defined in [UAX35] and MUST be interpreted by implementations as defined in that specification.

    type
        csvt:ToRdfTest
    approval
        rdft:Approved
    action
        test189-metadata.json 
    result
        test189.ttl 
    Implicit
        test189.csv 
    options
        noProv: true 

manifest-rdf#test190: date format (valid dateTime combinations with formats)

    The supported date and time formats listed here are expressed in terms of the date field symbols defined in [UAX35] and MUST be interpreted by implementations as defined in that specification.

    type
        csvt:ToRdfTest
    approval
        rdft:Approved
    action
        test190-metadata.json 
    result
        test190.ttl 
    Implicit
        test190.csv 
    options
        noProv: true 

manifest-rdf#test191: date format (bad format string)

    The supported date and time formats listed here are expressed in terms of the date field symbols defined in [UAX35] and MUST be interpreted by implementations as defined in that specification.

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test191-metadata.json 
    result
        test191.ttl 
    Implicit
        test191.csv 
    options
        noProv: true 

manifest-rdf#test192: date format (value not matching format)

    The supported date and time formats listed here are expressed in terms of the date field symbols defined in [UAX35] and MUST be interpreted by implementations as defined in that specification.

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test192-metadata.json 
    result
        test192.ttl 
    Implicit
        test192.csv 
    options
        noProv: true 

manifest-rdf#test193: duration format (valid combinations)

    If the datatype base is a duration type, the datatype format annotation provides a regular expression for the string values

    type
        csvt:ToRdfTest
    approval
        rdft:Approved
    action
        test193-metadata.json 
    result
        test193.ttl 
    Implicit
        test193.csv 
    options
        noProv: true 

manifest-rdf#test194: duration format (value not matching format)

    If the datatype base is a duration type, the datatype format annotation provides a regular expression for the string values

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test194-metadata.json 
    result
        test194.ttl 
    Implicit
        test194.csv 
    options
        noProv: true 

manifest-rdf#test195: values with matching length

    validate the value based on the length constraints described in section 4.6.1 Length Constraints, the value constraints described in section 4.6.2 Value Constraints and the datatype format annotation if one is specified, as described below. If there are any errors, add them to the list of errors for the cell.

    type
        csvt:ToRdfTest
    approval
        rdft:Approved
    action
        test195-metadata.json 
    result
        test195.ttl 
    Implicit
        test195.csv 
    options
        noProv: true 

manifest-rdf#test196: values with wrong length

    validate the value based on the length constraints described in section 4.6.1 Length Constraints, the value constraints described in section 4.6.2 Value Constraints and the datatype format annotation if one is specified, as described below. If there are any errors, add them to the list of errors for the cell.

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test196-metadata.json 
    result
        test196.ttl 
    Implicit
        test196.csv 
    options
        noProv: true 

manifest-rdf#test197: values with wrong maxLength

    validate the value based on the length constraints described in section 4.6.1 Length Constraints, the value constraints described in section 4.6.2 Value Constraints and the datatype format annotation if one is specified, as described below. If there are any errors, add them to the list of errors for the cell.

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test197-metadata.json 
    result
        test197.ttl 
    Implicit
        test197.csv 
    options
        noProv: true 

manifest-rdf#test198: values with wrong minLength

    validate the value based on the length constraints described in section 4.6.1 Length Constraints, the value constraints described in section 4.6.2 Value Constraints and the datatype format annotation if one is specified, as described below. If there are any errors, add them to the list of errors for the cell.

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test198-metadata.json 
    result
        test198.ttl 
    Implicit
        test198.csv 
    options
        noProv: true 

manifest-rdf#test199: length < minLength

    Applications MUST raise an error if both length and minLength are specified and length is less than minLength.

    Negative Test

    type
        csvt:NegativeRdfTest
    approval
        rdft:Approved
    action
        test199-metadata.json 
    Implicit
        test199.csv 
    options
        noProv: true 

manifest-rdf#test200: length > maxLength

    Applications MUST raise an error if both length and maxLength are specified and length is greater than maxLength.

    Negative Test

    type
        csvt:NegativeRdfTest
    approval
        rdft:Approved
    action
        test200-metadata.json 
    Implicit
        test200.csv 
    options
        noProv: true 

manifest-rdf#test201: length on date

    Applications MUST raise an error if length, maxLength, or minLength are specified and the base datatype is not string or one of its subtypes, or a binary type.

    Negative Test

    type
        csvt:NegativeRdfTest
    approval
        rdft:Approved
    action
        test201-metadata.json 
    Implicit
        test201.csv 
    options
        noProv: true 

manifest-rdf#test202: float matching constraints

    validate the value based on the length constraints described in section 4.6.1 Length Constraints, the value constraints described in section 4.6.2 Value Constraints and the datatype format annotation if one is specified, as described below. If there are any errors, add them to the list of errors for the cell.

    type
        csvt:ToRdfTest
    approval
        rdft:Approved
    action
        test202-metadata.json 
    result
        test202.ttl 
    Implicit
        test202.csv 
    options
        noProv: true 

manifest-rdf#test203: float value constraint not matching minimum

    validate the value based on the length constraints described in section 4.6.1 Length Constraints, the value constraints described in section 4.6.2 Value Constraints and the datatype format annotation if one is specified, as described below. If there are any errors, add them to the list of errors for the cell.

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test203-metadata.json 
    result
        test203.ttl 
    Implicit
        test203.csv 
    options
        noProv: true 

manifest-rdf#test204: float value constraint not matching maximum

    validate the value based on the length constraints described in section 4.6.1 Length Constraints, the value constraints described in section 4.6.2 Value Constraints and the datatype format annotation if one is specified, as described below. If there are any errors, add them to the list of errors for the cell.

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test204-metadata.json 
    result
        test204.ttl 
    Implicit
        test204.csv 
    options
        noProv: true 

manifest-rdf#test205: float value constraint not matching minInclusive

    validate the value based on the length constraints described in section 4.6.1 Length Constraints, the value constraints described in section 4.6.2 Value Constraints and the datatype format annotation if one is specified, as described below. If there are any errors, add them to the list of errors for the cell.

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test205-metadata.json 
    result
        test205.ttl 
    Implicit
        test205.csv 
    options
        noProv: true 

manifest-rdf#test206: float value constraint not matching minExclusive

    validate the value based on the length constraints described in section 4.6.1 Length Constraints, the value constraints described in section 4.6.2 Value Constraints and the datatype format annotation if one is specified, as described below. If there are any errors, add them to the list of errors for the cell.

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test206-metadata.json 
    result
        test206.ttl 
    Implicit
        test206.csv 
    options
        noProv: true 

manifest-rdf#test207: float value constraint not matching maxInclusive

    validate the value based on the length constraints described in section 4.6.1 Length Constraints, the value constraints described in section 4.6.2 Value Constraints and the datatype format annotation if one is specified, as described below. If there are any errors, add them to the list of errors for the cell.

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test207-metadata.json 
    result
        test207.ttl 
    Implicit
        test207.csv 
    options
        noProv: true 

manifest-rdf#test208: float value constraint not matching maxExclusive

    validate the value based on the length constraints described in section 4.6.1 Length Constraints, the value constraints described in section 4.6.2 Value Constraints and the datatype format annotation if one is specified, as described below. If there are any errors, add them to the list of errors for the cell.

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test208-metadata.json 
    result
        test208.ttl 
    Implicit
        test208.csv 
    options
        noProv: true 

manifest-rdf#test209: date matching constraints

    validate the value based on the length constraints described in section 4.6.1 Length Constraints, the value constraints described in section 4.6.2 Value Constraints and the datatype format annotation if one is specified, as described below. If there are any errors, add them to the list of errors for the cell.

    type
        csvt:ToRdfTest
    approval
        rdft:Approved
    action
        test209-metadata.json 
    result
        test209.ttl 
    Implicit
        test209.csv 
    options
        noProv: true 

manifest-rdf#test210: date value constraint not matching minimum

    validate the value based on the length constraints described in section 4.6.1 Length Constraints, the value constraints described in section 4.6.2 Value Constraints and the datatype format annotation if one is specified, as described below. If there are any errors, add them to the list of errors for the cell.

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test210-metadata.json 
    result
        test210.ttl 
    Implicit
        test210.csv 
    options
        noProv: true 

manifest-rdf#test211: date value constraint not matching maximum

    validate the value based on the length constraints described in section 4.6.1 Length Constraints, the value constraints described in section 4.6.2 Value Constraints and the datatype format annotation if one is specified, as described below. If there are any errors, add them to the list of errors for the cell.

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test211-metadata.json 
    result
        test211.ttl 
    Implicit
        test211.csv 
    options
        noProv: true 

manifest-rdf#test212: date value constraint not matching minInclusive

    validate the value based on the length constraints described in section 4.6.1 Length Constraints, the value constraints described in section 4.6.2 Value Constraints and the datatype format annotation if one is specified, as described below. If there are any errors, add them to the list of errors for the cell.

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test212-metadata.json 
    result
        test212.ttl 
    Implicit
        test212.csv 
    options
        noProv: true 

manifest-rdf#test213: date value constraint not matching minExclusive

    validate the value based on the length constraints described in section 4.6.1 Length Constraints, the value constraints described in section 4.6.2 Value Constraints and the datatype format annotation if one is specified, as described below. If there are any errors, add them to the list of errors for the cell.

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test213-metadata.json 
    result
        test213.ttl 
    Implicit
        test213.csv 
    options
        noProv: true 

manifest-rdf#test214: date value constraint not matching maxInclusive

    validate the value based on the length constraints described in section 4.6.1 Length Constraints, the value constraints described in section 4.6.2 Value Constraints and the datatype format annotation if one is specified, as described below. If there are any errors, add them to the list of errors for the cell.

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test214-metadata.json 
    result
        test214.ttl 
    Implicit
        test214.csv 
    options
        noProv: true 

manifest-rdf#test215: date value constraint not matching maxExclusive

    validate the value based on the length constraints described in section 4.6.1 Length Constraints, the value constraints described in section 4.6.2 Value Constraints and the datatype format annotation if one is specified, as described below. If there are any errors, add them to the list of errors for the cell.

    Expects Warnings

    type
        csvt:ToRdfTestWithWarnings
    approval
        rdft:Approved
    action
        test215-metadata.json 
    result
        test215.ttl 
    Implicit
        test215.csv 
    options
        noProv: true 

manifest-rdf#test216: minInclusive and minExclusive

    Applications MUST raise an error if both minInclusive and minExclusive are specified, or if both maxInclusive and maxExclusive are specified.

    Negative Test

    type
        csvt:NegativeRdfTest
    approval
        rdft:Approved
    action
        test216-metadata.json 
    Implicit
        test216.csv 
    options
        noProv: true 

manifest-rdf#test217: maxInclusive and maxExclusive

    Applications MUST raise an error if both minInclusive and minExclusive are specified, or if both maxInclusive and maxExclusive are specified.

    Negative Test

    type
        csvt:NegativeRdfTest
    approval
        rdft:Approved
    action
        test217-metadata.json 
    Implicit
        test217.csv 
    options
        noProv: true 

manifest-rdf#test218: maxInclusive < minInclusive

    Applications MUST raise an error if both minInclusive and maxInclusive are specified and maxInclusive is less than minInclusive, or if both minInclusive and maxExclusive are specified and maxExclusive is less than or equal to minInclusive.

    Negative Test

    type
        csvt:NegativeRdfTest
    approval
        rdft:Approved
    action
        test218-metadata.json 
    Implicit
        test218.csv 
    options
        noProv: true 

manifest-rdf#test219: maxExclusive = minInclusive

    Applications MUST raise an error if both minInclusive and maxInclusive are specified and maxInclusive is less than minInclusive, or if both minInclusive and maxExclusive are specified and maxExclusive is less than or equal to minInclusive.

    Negative Test

    type
        csvt:NegativeRdfTest
    approval
        rdft:Approved
    action
        test219-metadata.json 
    Implicit
        test219.csv 
    options
        noProv: true 

manifest-rdf#test220: maxExclusive < minExclusive

    Applications MUST raise an error if both minExclusive and maxExclusive are specified and maxExclusive is less than minExclusive, or if both minExclusive and maxInclusive are specified and maxInclusive is less than or equal to minExclusive.

    Negative Test

    type
        csvt:NegativeRdfTest
    approval
        rdft:Approved
    action
        test220-metadata.json 
    Implicit
        test220.csv 
    options
        noProv: true 

manifest-rdf#test221: maxInclusive = minExclusive

    Applications MUST raise an error if both minExclusive and maxExclusive are specified and maxExclusive is less than minExclusive, or if both minExclusive and maxInclusive are specified and maxInclusive is less than or equal to minExclusive.

    Negative Test

    type
        csvt:NegativeRdfTest
    approval
        rdft:Approved
    action
        test221-metadata.json 
    Implicit
        test221.csv 
    options
        noProv: true 

manifest-rdf#test222: string datatype with minimum

    Applications MUST raise an error if minimum, minInclusive, maximum, maxInclusive, minExclusive, or maxExclusive are specified and the base datatype is not a numeric, date/time, or duration type.

    Negative Test

    type
        csvt:NegativeRdfTest
    approval
        rdft:Approved
    action
        test222-metadata.json 
    Implicit
        test222.csv 
    options
        noProv: true 

manifest-rdf#test223: string datatype with maxium

    Applications MUST raise an error if minimum, minInclusive, maximum, maxInclusive, minExclusive, or maxExclusive are specified and the base datatype is not a numeric, date/time, or duration type.

    Negative Test

    type
        csvt:NegativeRdfTest
    approval
        rdft:Approved
    action
        test223-metadata.json 
    Implicit
        test223.csv 
    options
        noProv: true 

manifest-rdf#test224: string datatype with minInclusive

    Applications MUST raise an error if minimum, minInclusive, maximum, maxInclusive, minExclusive, or maxExclusive are specified and the base datatype is not a numeric, date/time, or duration type.

    Negative Test

    type
        csvt:NegativeRdfTest
    approval
        rdft:Approved
    action
        test224-metadata.json 
    Implicit
        test224.csv 
    options
        noProv: true 

manifest-rdf#test225: string datatype with maxInclusive

    Applications MUST raise an error if minimum, minInclusive, maximum, maxInclusive, minExclusive, or maxExclusive are specified and the base datatype is not a numeric, date/time, or duration type.

    Negative Test

    type
        csvt:NegativeRdfTest
    approval
        rdft:Approved
    action
        test225-metadata.json 
    Implicit
        test225.csv 
    options
        noProv: true 

manifest-rdf#test226: string datatype with minExclusive

    Applications MUST raise an error if minimum, minInclusive, maximum, maxInclusive, minExclusive, or maxExclusive are specified and the base datatype is not a numeric, date/time, or duration type.

    Negative Test

    type
        csvt:NegativeRdfTest
    approval
        rdft:Approved
    action
        test226-metadata.json 
    Implicit
        test226.csv 
    options
        noProv: true 

manifest-rdf#test227: string datatype with maxExclusive

    Applications MUST raise an error if minimum, minInclusive, maximum, maxInclusive, minExclusive, or maxExclusive are specified and the base datatype is not a numeric, date/time, or duration type.

    Negative Test

    type
        csvt:NegativeRdfTest
    approval
        rdft:Approved
    action
        test227-metadata.json 
    Implicit
        test227.csv 
    options
        noProv: true 

manifest-rdf#test228: length with separator

    If the value is a list, the constraint applies to each element of the list.

manifest-rdf#test229: matching minLength with separator

    If the value is a list, the constraint applies to each element of the list.

manifest-rdf#test230: failing minLength with separator

    If the value is a list, the constraint applies to each element of the list.

manifest-rdf#test231: single column primaryKey success

    As defined in [tabular-data-model], validators MUST check that each row has a unique combination of values of cells in the indicated columns.

manifest-rdf#test232: single column primaryKey violation

    Validators MUST raise errors if there is more than one row with the same primary key

manifest-rdf#test233: multiple column primaryKey success

    As defined in [tabular-data-model], validators MUST check that each row has a unique combination of values of cells in the indicated columns.

manifest-rdf#test234: multiple column primaryKey violation

    Validators MUST raise errors if there is more than one row with the same primary key

manifest-rdf#test235: rowTitles on one column

    if row titles is not null, insert any titles specified for the row. For each value, tv, of the row titles annotation

manifest-rdf#test236: rowTitles on multiple columns

    if row titles is not null, insert any titles specified for the row. For each value, tv, of the row titles annotation

manifest-rdf#test237: rowTitles on one column (minimal)

    if row titles is not null, insert any titles specified for the row. For each value, tv, of the row titles annotation

manifest-rdf#test238: datatype value an absolute URL

    it must be the name of one of the built-in datatypes defined in section 5.11.1 Built-in Datatypes

manifest-rdf#test242: datatype @id an absolute URL

    If included, @id is a link property that identifies the datatype described by this datatype description.

manifest-rdf#test243: invalid datatype @id

    It MUST NOT start with _:.

    Negative Test

manifest-rdf#test244: invalid datatype @id

    It MUST NOT be the URL of a built-in datatype.

    Negative Test

manifest-rdf#test245: date format (valid time combinations with formats and milliseconds)

    The supported date and time formats listed here are expressed in terms of the date field symbols defined in [UAX35] and MUST be interpreted by implementations as defined in that specification.

manifest-rdf#test246: date format (valid dateTime combinations with formats and milliseconds)

    The supported date and time formats listed here are expressed in terms of the date field symbols defined in [UAX35] and MUST be interpreted by implementations as defined in that specification.

manifest-rdf#test247: date format (extra milliseconds)

    The supported date and time formats listed here are expressed in terms of the date field symbols defined in [UAX35] and MUST be interpreted by implementations as defined in that specification.

manifest-rdf#test248: Unicode in non-Normalized form

    No Unicode normalization (as specified in [UAX15]) is applied to these string values

manifest-rdf#test251: missing source reference

    As defined in [tabular-data-model], validators MUST check that, for each row, the combination of cells in the referencing columns references a unique row within the referenced table through a combination of cells in the referenced columns.

    Negative Test

manifest-rdf#test252: missing destination reference column

    As defined in [tabular-data-model], validators MUST check that, for each row, the combination of cells in the referencing columns references a unique row within the referenced table through a combination of cells in the referenced columns.

    Negative Test

nifest-rdf#test253: missing destination table

    As defined in [tabular-data-model], validators MUST check that, for each row, the combination of cells in the referencing columns references a unique row within the referenced table through a combination of cells in the referenced columns.

    Negative Test

manifest-rdf#test259: tree-ops example with csvm.json (w3.org/.well-known/csvm)

    Processors MUST use the first metadata found for processing a tabular data file by using overriding metadata, if provided. Otherwise processors MUST attempt to locate the first metadata document from the Link header or the metadata located through site-wide configuration.

manifest-rdf#test260: tree-ops example with {+url}.json (w3.org/.well-known/csvm)

    Processors MUST use the first metadata found for processing a tabular data file by using overriding metadata, if provided. Otherwise processors MUST attempt to locate the first metadata document from the Link header or the metadata located through site-wide configuration.

manifest-rdf#test261: maxLength < minLength

    Applications MUST raise an error if both minLength and maxLength are specified and minLength is greater than maxLength.

    Negative Test

manifest-rdf#test263: @type on a common property can be a built-in type

    The value of any member of @type MUST be either a term defined in [csvw-context], a prefixed name where the prefix is a term defined in [csvw-context], or an absolute URL.

manifest-rdf#test264: @type on a common property can be a CURIE if the prefix is one of the built-in ones

    The value of any member of @type MUST be either a term defined in [csvw-context], a prefixed name where the prefix is a term defined in [csvw-context], or an absolute URL.

manifest-rdf#test266: `null` contains an array of (valid) string & (invalid) numeric values

    Processors MUST issue a warning if a property is set to an invalid value type

manifest-rdf#test267: @id on datatype is invalid (eg starts with _:)

    It MUST NOT start with _: and it MUST NOT be the URL of a built-in datatype.

manifest-rdf#test268: `base` missing on datatype (defaults to string)

    An atomic property that contains a single string: the name of one of the built-in datatypes, as listed above (and which are defined as terms in the default context). Its default is string.

manifest-rdf#test269: `format` for a boolean datatype is a string but in the wrong form (eg YN)

    If the datatype base for a cell is boolean, the datatype format annotation provides the true value followed by the false value, separated by |. If the format does not follow this syntax, implementations MUST issue a warning and proceed as if no format had been provided.

manifest-rdf#test270: transformation includes an invalid property (eg foo)

    All terms used within a metadata document MUST be defined in [csvw-context] defined for this specification

manifest-rdf#test271: foreign key includes an invalid property (eg `dc:description`)

    A foreign key definition is a JSON object that must contain only the following properties. . .

    Negative Test

manifest-rdf#test272: foreign key reference includes an invalid property (eg `dc:description`)

    A foreign key definition is a JSON object that must contain only the following properties. . .

    Negative Test

manifest-rdf#test273: `@base` set in `@context` overriding eg CSV location

    If present, its value MUST be a string that is interpreted as a URL which is resolved against the location of the metadata document to provide the base URL for other URLs in the metadata document.

manifest-rdf#test274: `@context` object includes properties other than `@base` and `@language`

    The @context MUST have one of the following values: An array composed of a string followed by an object, where the string is http://www.w3.org/ns/csvw and the object represents a local context definition, which is restricted to contain either or both of the following members.

    Negative Test

manifest-rdf#test275: property acceptable on column appears on table group

    Table Group may only use defined properties.

manifest-rdf#test276: property acceptable on column appears on table

    Table may only use defined properties.

manifest-rdf#test277: property acceptable on table appears on column

    Column may only use defined properties.

manifest-rdf#test278: CSV has more headers than there are columns in the metadata

    Two schemas are compatible if they have the same number of non-virtual column descriptions, and the non-virtual column descriptions at the same index within each are compatible with each other.

manifest-rdf#test279: duration not matching xsd pattern

    Value MUST be a valid xsd:duration.

manifest-rdf#test280: dayTimeDuration not matching xsd pattern

    Value MUST be a valid xsd:dayTimeDuration.

manifest-rdf#test281: yearMonthDuration not matching xsd pattern

    Value MUST be a valid xsd:yearMonthDuration.

manifest-rdf#test282: valid number patterns

    A number format pattern as defined in [UAX35]. Implementations MUST recognise number format patterns containing the symbols 0, #, the specified decimalChar (or . if unspecified), the specified groupChar (or , if unspecified), E, +, % and &permil;.

manifest-rdf#test283: valid number patterns (signs and percent/permille)

    A number format pattern as defined in [UAX35]. Implementations MUST recognise number format patterns containing the symbols 0, #, the specified decimalChar (or . if unspecified), the specified groupChar (or , if unspecified), E, +, % and &permil;.

manifest-rdf#test284: valid number patterns (grouping)

    A number format pattern as defined in [UAX35]. Implementations MUST recognise number format patterns containing the symbols 0, #, the specified decimalChar (or . if unspecified), the specified groupChar (or , if unspecified), E, +, % and &permil;.

manifest-rdf#test285: valid number patterns (fractional grouping)

    A number format pattern as defined in [UAX35]. Implementations MUST recognise number format patterns containing the symbols 0, #, the specified decimalChar (or . if unspecified), the specified groupChar (or , if unspecified), E, +, % and &permil;.

manifest-rdf#test286: invalid ##0 1,234

    A number format pattern as defined in [UAX35]. Implementations MUST recognise number format patterns containing the symbols 0, #, the specified decimalChar (or . if unspecified), the specified groupChar (or , if unspecified), E, +, % and &permil;.

manifest-rdf#test287: invalid ##0 123.4

    A number format pattern as defined in [UAX35]. Implementations MUST recognise number format patterns containing the symbols 0, #, the specified decimalChar (or . if unspecified), the specified groupChar (or , if unspecified), E, +, % and &permil;.

manifest-rdf#test288: invalid #,#00 1

    A number format pattern as defined in [UAX35]. Implementations MUST recognise number format patterns containing the symbols 0, #, the specified decimalChar (or . if unspecified), the specified groupChar (or , if unspecified), E, +, % and &permil;.

manifest-rdf#test289: invalid #,#00 1234

    A number format pattern as defined in [UAX35]. Implementations MUST recognise number format patterns containing the symbols 0, #, the specified decimalChar (or . if unspecified), the specified groupChar (or , if unspecified), E, +, % and &permil;.

manifest-rdf#test290: invalid #,#00 12,34

    A number format pattern as defined in [UAX35]. Implementations MUST recognise number format patterns containing the symbols 0, #, the specified decimalChar (or . if unspecified), the specified groupChar (or , if unspecified), E, +, % and &permil;.

manifest-rdf#test291: invalid #,#00 12,34,567

    A number format pattern as defined in [UAX35]. Implementations MUST recognise number format patterns containing the symbols 0, #, the specified decimalChar (or . if unspecified), the specified groupChar (or , if unspecified), E, +, % and &permil;.

manifest-rdf#test292: invalid #,##,#00 1

    A number format pattern as defined in [UAX35]. Implementations MUST recognise number format patterns containing the symbols 0, #, the specified decimalChar (or . if unspecified), the specified groupChar (or , if unspecified), E, +, % and &permil;.

manifest-rdf#test293: invalid #,##,#00 1234

    A number format pattern as defined in [UAX35]. Implementations MUST recognise number format patterns containing the symbols 0, #, the specified decimalChar (or . if unspecified), the specified groupChar (or , if unspecified), E, +, % and &permil;.

manifest-rdf#test294: invalid #,##,#00 12,34

    A number format pattern as defined in [UAX35]. Implementations MUST recognise number format patterns containing the symbols 0, #, the specified decimalChar (or . if unspecified), the specified groupChar (or , if unspecified), E, +, % and &permil;.

manifest-rdf#test295: invalid #,##,#00 1,234,567

    A number format pattern as defined in [UAX35]. Implementations MUST recognise number format patterns containing the symbols 0, #, the specified decimalChar (or . if unspecified), the specified groupChar (or , if unspecified), E, +, % and &permil;.

manifest-rdf#test296: invalid #0.# 12.34

    A number format pattern as defined in [UAX35]. Implementations MUST recognise number format patterns containing the symbols 0, #, the specified decimalChar (or . if unspecified), the specified groupChar (or , if unspecified), E, +, % and &permil;.

manifest-rdf#test297: invalid #0.# 1,234.5

    A number format pattern as defined in [UAX35]. Implementations MUST recognise number format patterns containing the symbols 0, #, the specified decimalChar (or . if unspecified), the specified groupChar (or , if unspecified), E, +, % and &permil;.

manifest-rdf#test298: invalid #0.0 1

    A number format pattern as defined in [UAX35]. Implementations MUST recognise number format patterns containing the symbols 0, #, the specified decimalChar (or . if unspecified), the specified groupChar (or , if unspecified), E, +, % and &permil;.

manifest-rdf#test299: invalid #0.0 12.34

    A number format pattern as defined in [UAX35]. Implementations MUST recognise number format patterns containing the symbols 0, #, the specified decimalChar (or . if unspecified), the specified groupChar (or , if unspecified), E, +, % and &permil;.

manifest-rdf#test300: invalid #0.0# 1

    A number format pattern as defined in [UAX35]. Implementations MUST recognise number format patterns containing the symbols 0, #, the specified decimalChar (or . if unspecified), the specified groupChar (or , if unspecified), E, +, % and &permil;.

manifest-rdf#test301: invalid #0.0# 12.345

    A number format pattern as defined in [UAX35]. Implementations MUST recognise number format patterns containing the symbols 0, #, the specified decimalChar (or . if unspecified), the specified groupChar (or , if unspecified), E, +, % and &permil;.

manifest-rdf#test302: invalid #0.0#,# 1

    A number format pattern as defined in [UAX35]. Implementations MUST recognise number format patterns containing the symbols 0, #, the specified decimalChar (or . if unspecified), the specified groupChar (or , if unspecified), E, +, % and &permil;.

manifest-rdf#test303: invalid #0.0#,# 12.345

    A number format pattern as defined in [UAX35]. Implementations MUST recognise number format patterns containing the symbols 0, #, the specified decimalChar (or . if unspecified), the specified groupChar (or , if unspecified), E, +, % and &permil;.

manifest-rdf#test304: invalid #0.0#,# 12.34,567

    A number format pattern as defined in [UAX35]. Implementations MUST recognise number format patterns containing the symbols 0, #, the specified decimalChar (or . if unspecified), the specified groupChar (or , if unspecified), E, +, % and &permil;.

manifest-rdf#test305: multiple values with same subject and property (unordered)

    Values in separate columns using the same propertyUrl are kept in proper relative order.

manifest-rdf#test306: multiple values with same subject and property (ordered)

    Values in separate columns using the same propertyUrl are kept in proper relative order.

manifest-rdf#test307: multiple values with same subject and property (ordered and unordered)

    Values in separate columns using the same propertyUrl are kept in proper relative order.


# fhirvc

tool that allows you to compare metadata of fhir versions

# TODO
* Switch to offline generation of JSON difference
(due to practical inefficiency of online version)
* consider adding parallel execution of JSON difference
* get rid of empty "added" and "removed" properties in case if structure has not changed

## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    lein ring server

## License

Copyright © 2016 FIXME

package filters

import javax.inject.Inject

import play.api.http.DefaultHttpFilters

class Filters @Inject()(
                         restErrorFilter: RestErrorFilter,
                         log: LoggingFilter
                       ) extends DefaultHttpFilters(restErrorFilter, log)
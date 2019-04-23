FROM ruby:2.5.3

COPY Gemfile Gemfile
COPY Gemfile.lock Gemfile.lock

RUN gem install bundler
RUN bundle install

COPY features /features
COPY wait-for-it.sh /

ENTRYPOINT ["./wait-for-it.sh", "http://selenium-hub:4444/status", "--", "bundle", "exec", "cucumber", "--strict", "--tags", "not @ignore"]

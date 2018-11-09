FROM ruby:2.4.2

COPY Gemfile Gemfile
COPY Gemfile.lock Gemfile.lock

RUN bundle install

COPY features /features

ENTRYPOINT ["bundle", "exec", "cucumber", "--strict"]

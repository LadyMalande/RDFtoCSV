foo = gets.chomp
puts "Input was: " + foo
begin
  gem "rdf/tabular"
  # with requirements
  gem "rdf/tabular", ">=2.0"
rescue Gem::LoadError
  puts "not installed"
end
rdf serialize --input-format tabular --output-format turtle --minimal app.csv
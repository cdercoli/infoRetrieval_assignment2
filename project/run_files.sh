echo "running script..."
mvn package
echo ""

echo "running jar..."
java -jar target/assignment2-1.2.jar
echo ""

echo "running trec_eval..."
./trec_eval-9.0.7/trec_eval qrels/qrels.assignment2.part1 results/query_results.txt
echo ""

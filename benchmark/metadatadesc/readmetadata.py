import sys
import json

if len(sys.argv) < 2:
	print 'Usage: ' + sys.argv[0] + ' <JSON metada file>'
	sys.exit()

JSONFileName = sys.argv[1]

data = json.loads(open(JSONFileName).read())
for proj in data['items']:
	print proj['name'] + ';' + proj['full_name'] + ';' + (proj['description'].replace(';',',').replace('\n',' ') if 'description' in proj and proj['description'] is not None else '')



#!/bin/bash -e
destination="$1"
sourceDirs="$2"

# Function to copy files for avoiding duplicates
copy_files() {
    source_dir="$1"
    db="${source_dir##*-}"
    for file in "$source_dir"/*; do
        # Get the file name and extension
        filename=$(basename "$file")
        extension="${filename##*.}"
        filename_without_extension="${filename%.*}"

        #Forming new filename based on DB name
        filename="$filename_without_extension-$db.$extension"

        # Copy the file to the destination
        cp "$file" "$destination/$filename"
    done
}

for i in $sourceDirs; do
    echo "Copying $i"
    copy_files $i
done
echo "Merging complete!"
package main

import (
	"fmt"
	"os"

	"golang.org/x/crypto/bcrypt"
)

func main() {
	if len(os.Args) < 3 {
		fmt.Fprintf(os.Stderr, "Usage: %s <password> <hash>\n", os.Args[0])
		os.Exit(1)
	}

	password := os.Args[1]
	hash := os.Args[2]

	err := bcrypt.CompareHashAndPassword([]byte(hash), []byte(password))
	if err == nil {
		fmt.Println("Password matches hash!")
		os.Exit(0)
	} else {
		fmt.Printf("Password does NOT match hash: %v\n", err)
		os.Exit(1)
	}
}

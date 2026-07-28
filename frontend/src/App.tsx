import {useEffect, useState} from "react";

function App() {
    const [helloWorld, setHelloWorld] = useState(':(');

    useEffect(() => {
        fetch('http://localhost:8080/api/hello')
            .then(res => res.text())
            .then(data => setHelloWorld(data))
    }, [])

    return (
        <div>
            <p>{helloWorld}</p>
        </div>
    )
}

export default App;